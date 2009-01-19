/*
 * Copyright (C) 2008 Alexander Christian <alex(at)root1.de>. All rights reserved.
 * 
 * This file is part of SIMON.
 *
 *   SIMON is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SIMON is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with SIMON.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.root1.simon;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;

import javax.net.ssl.SSLContext;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.executor.OrderedThreadPoolExecutor;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.filter.ssl.SslFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.root1.simon.codec.base.SimonProtocolCodecFactory;
import de.root1.simon.exceptions.LookupFailedException;
import de.root1.simon.exceptions.NameBindingException;
import de.root1.simon.ssl.SslContextFactory;
import de.root1.simon.utils.Utils;

/**
 * The SIMON server acts as a registry for remote objects. So, Registry is
 * SIMON's internal server implementation
 * 
 * @author achristian
 * 
 */
public final class Registry {
	
	/**
	 * TODO document me
	 */
	private final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * The address in which the registry is listening
	 */
	private InetAddress address;

	/**
	 * The port on which the registry is listening
	 */
	private int port;

	/**
	 * The Distaptcher for all incoming connections
	 */
	private Dispatcher dispatcher;
	
	/**
	 * the socket acceptor
	 */
	private IoAcceptor acceptor;
	
	/** The pool in which the dispatcher, acceptor and registry lives */
	private ExecutorService threadPool;

	/**
	 * A thread pool for the filterchain => more performance
	 */
	private ExecutorService filterchainWorkerPool;
	
	/**
	 * Name of the protocol factory to use
	 */
	private String protocolFactoryClassName;

	private SslContextFactory sslContextFactory;

	/**
	 * Creates a registry 
	 * 
	 * @param address the interface address on which the socketserver listens on
	 * @param port the port on which the socketserver listens on
	 * @param threadPool the thread pool implementation which is forwarded to the dispatcher
	 * @param protocolFactoryClassName the full classname of the class that describes to network protocol
	 * @throws IOException if there are problems with creating the mina socketserver
	 */
	protected Registry(InetAddress address, int port, ExecutorService threadPool, String protocolFactoryClassName) throws IOException {
		logger.debug("begin");
		this.address  = address;
		this.port = port;
		this.threadPool = threadPool;
		this.protocolFactoryClassName = protocolFactoryClassName;
		start();
		logger.debug("end");
	}
	
	/**
	 * Creates a SSL powered registry
	 * 
	 * @param address the interface address on which the socketserver listens on
	 * @param port the port on which the socketserver listens on
	 * @param threadPool the thread pool implementation which is forwarded to the dispatcher
	 * @param protocolFactoryClassName the full classname of the class that describes to network protocol
	 * @param sslContextFactory the factory which is used to get the server ssl context
	 * @throws IOException if there are problems with creating the mina socketserver
	 */
	protected Registry(InetAddress address, int port, ExecutorService threadPool, String protocolFactoryClassName, SslContextFactory sslContextFactory) throws IOException {
		logger.debug("begin");
		this.address  = address;
		this.port = port;
		this.threadPool = threadPool;
		this.protocolFactoryClassName = protocolFactoryClassName;
		this.sslContextFactory = sslContextFactory;
		start();
		logger.debug("end");
	}

	/**
	 * Starts the registry thread
	 * 
	 * @throws IOException
	 *             if there's a problem getting a selector for the non-blocking
	 *             network communication, or if the
	 * 
	 */
	private void start() throws IOException {

		logger.debug("begin");
					
		dispatcher = new Dispatcher(null, threadPool);
		logger.debug("dispatcher created");
		
		acceptor = new NioSocketAcceptor();
		
		if (acceptor instanceof NioSocketAcceptor) {
			NioSocketAcceptor nioSocketAcceptor = (NioSocketAcceptor) acceptor;
			
			logger.debug("setting 'TcpNoDelay' on NioSocketAcceptor");
			nioSocketAcceptor.getSessionConfig().setTcpNoDelay(true);

			logger.debug("setting 'ReuseAddress' on NioSocketAcceptor");
			nioSocketAcceptor.setReuseAddress(true);
		}
		
		// only add the logging filter if trace is enabled
		if (logger.isTraceEnabled())
			acceptor.getFilterChain().addLast( "logger", new LoggingFilter() );

		
		if (sslContextFactory!=null) {
				SSLContext context = sslContextFactory.getSslContext();
				
				if (context!=null) {
					SslFilter sslFilter = new SslFilter(context);
					acceptor.getFilterChain().addLast("sslFilter", sslFilter);
					logger.debug("SSL ON");
				} else {
					logger.warn("SSLContext retrieved from SslContextFactory was 'null', so starting WITHOUT SSL!");
				}
		}

		filterchainWorkerPool = new OrderedThreadPoolExecutor();
		acceptor.getFilterChain().addLast("executor", new ExecutorFilter(filterchainWorkerPool));
		

		SimonProtocolCodecFactory protocolFactory = null;
		try {
			protocolFactory = Utils.getProtocolFactoryInstance(protocolFactoryClassName);
		} catch (ClassNotFoundException e) {
			logger.warn("this should never happen. Please contact author. -> {}", e.getMessage());
			// already proved
		} catch (InstantiationException e) {
			// already proved
			logger.warn("this should never happen. Please contact author. -> {}", e.getMessage());
		} catch (IllegalAccessException e) {
			// already proved
			logger.warn("this should never happen. Please contact author. -> {}", e.getMessage());
		}
		protocolFactory.setup(true);
		acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(protocolFactory));
		
		
		acceptor.setHandler(dispatcher);
		logger.trace("Configuring acceptor with default values: write_timeout={}sec dgc_interval={}sec",Statics.DEFAULT_WRITE_TIMEOUT, Statics.DEFAULT_IDLE_TIME);
        setDgcInterval(Statics.DEFAULT_IDLE_TIME);
        setWriteTimeout(Statics.DEFAULT_WRITE_TIMEOUT);
        
        
        logger.trace("Listening on {} on port {}",address,port);
    	acceptor.bind(new InetSocketAddress(address, port));
        
        
        logger.debug("acceptor thread created and started");			
        logger.debug("end");
	}

	/**
	 * TODO document me
	 * @param timeout
	 */
	public void setWriteTimeout(int timeout) {
		acceptor.getSessionConfig().setWriteTimeout(timeout);
		dispatcher.setPingTimeOut(timeout);
		logger.debug("setting write timeout to {} sec.", timeout);
	}

	/**
	 * TODO document me
	 * @param interval
	 */
	public void setDgcInterval(int interval) {
		acceptor.getSessionConfig().setIdleTime( IdleStatus.BOTH_IDLE, interval );
		logger.debug("setting dgc interval to {} sec.", interval);
	}
	
	/**
	 * TODO document me
	 */
	public int getWriteTimeout() {
		return acceptor.getSessionConfig().getWriteTimeout();
	}

	/**
	 * TODO document me
	 */
	public int getDgcInterval() {
		return acceptor.getSessionConfig().getIdleTime( IdleStatus.BOTH_IDLE );
	}
	
	/**
	 * Stops the registry. This clears the {@link LookupTable}, stops the
	 * acceptor and the {@link Dispatcher}. After running this method, no
	 * further connection/communication is possible with this registry.
	 * 
	 */
	public void stop() {
		logger.trace("begin");
		
		logger.trace("Unbind Acceptor ...");
		acceptor.unbind();
		
		logger.trace("Shutdown Dispatcher ...");
		dispatcher.shutdown();
		
		logger.trace("Dispose Acceptor ...");
		acceptor.dispose();
		
		logger.trace("Shutdown FilterchainWorkerPool ...");
		filterchainWorkerPool.shutdown();

// FIXME what was the intentionm to disable this?		
//		logger.trace("Clearing LookupTable ...");
//		lookupTableServer.cleanup();
		
		logger.trace("end");
	}
	
	/**
	 * Binds a remote object to the registry's own {@link LookupTable}
	 * 
	 * @param name
	 *            a name for object to bind
	 * @param remoteObject
	 *            the object to bind
	 * @throws NameBindingException
	 *             if there are problems binding the remoteobject to the
	 *             registry
	 */
	public void bind(String name, SimonRemote remoteObject) throws NameBindingException {
		try {
			if (dispatcher.getLookupTable().getRemoteBinding(name)!=null) 
				throw new NameBindingException("a remote object with the name '"+name+"' is already bound to this registry. unbind() first, or alternatively rebind().");
		} catch (LookupFailedException e) {
			// nothing to do
		}
		dispatcher.getLookupTable().putRemoteBinding(name, remoteObject);
	}
	
	/**
	 * Binds the object to the {@link Registry} and publishes it to the network,
	 * so that they can be found with {@link Simon#searchRemoteObjects(int)} or
	 * {@link Simon#searchRemoteObjects(SearchProgressListener, int)}
	 * 
	 * @param name
	 *            a name for the object to bind and publish
	 * @param remoteObject
	 *            the object to bind and publish
	 * @throws NameBindingException
	 *             if binding fails
	 */
	public void bindAndPublish(String name, SimonRemote remoteObject) throws NameBindingException {
		bind(name, remoteObject);
		try {
			Simon.publish(new SimonPublication(address, port, name));
		} catch (IOException e) {
			unbind(name);
			throw new NameBindingException("can't publish '"+name+"'. object is not bind! error="+e.getMessage());
		}
	}
	
	/**
	 * Unbinds a remote object from the registry's own {@link LookupTable}. If
	 * it's published, it's removed from the list of published objects
	 * 
	 * @param name
	 *            the object to unbind (and unpublish, if published)
	 * @return true, if unpublish succeeded, false, if object wasn't published
	 *         and though can't be unpublished
	 */
	public boolean unbind(String name){
		//TODO what to do with already connected users?
		dispatcher.getLookupTable().releaseRemoteBinding(name);
		return Simon.unpublish(new SimonPublication(address, port, name));
	}
	
	/**
	 * Unpublish a already published remote object.
	 * 
	 * @param name
	 *            the object to unpublish, if published
	 * 
	 * @return true, if unpublish succeeded, false, if object wasn't published
	 *         and though can't be unpublished
	 */
	public boolean unpublish(String name){
		return Simon.unpublish(new SimonPublication(address, port, name));
	}
	
	/**
	 * As the name says, it re-binds a remote object. This method shows the same
	 * behavior as the following two commands in sequence:<br>
	 * <br>
	 * <code>
	 * unbind(name);<br>
	 * bind(name, remoteObject);
	 * </code>
	 * 
	 * @param name
	 *            the name of the object to rebind
	 * @param remoteObject
	 *            the object to rebind
	 */
	public void rebind(String name, SimonRemote remoteObject){
		unbind(name);
		try {
			bind(name, remoteObject);
		} catch (NameBindingException e) {
			// this should never happen, nevertheless, we log it
			logger.warn("rebind() should never throw an NameBindingException. Contact SIMON author and send him this log.");
		}
	}
	
	/**
	 * Returns whether the registry is running and active or not
	 * 
	 * @return boolean
	 */
	public boolean isRunning(){
		return (dispatcher.isRunning() || acceptor.isActive() || !filterchainWorkerPool.isTerminated());
	}
	
	/**
	 * Returns a object that lets you get some network related information on
	 * the session of the given remote object (an instance of {@link SimonProxy}
	 * 
	 * @return an implementation of {@link SimonRegistryStatistics} that gives
	 *         access to the statistics data of this {@link Registry}
	 */
	public SimonRegistryStatistics getStatistics(){
		return new RegistryStatistics(acceptor.getStatistics());
	}

	/**
	 * Returns the {@link Dispatcher} associated with this registry.
	 * @return the related dispatcher
	 */
	protected Dispatcher getDispatcher() {
		return dispatcher;
	}
	
}
