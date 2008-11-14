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
import java.util.concurrent.Executors;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.executor.OrderedThreadPoolExecutor;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.root1.simon.codec.base.SimonProtocolCodecFactory;
import de.root1.simon.exceptions.LookupFailedException;
import de.root1.simon.exceptions.NameBindingException;
import de.root1.simon.utils.Utils;

/**
 * The SIMON server acts as a registry for remote objects. So, Registry is
 * SIMON's internal server implementation
 * 
 * @author achristian
 * 
 */
public class Registry {
	
	/**
	 * TODO document me
	 */
private final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * TODO document me
	 */
	private LookupTable lookupTableServer;

	/**
	 * TODO document me
	 */
	private InetAddress address;

	/**
	 * TODO document me
	 */
	private int port;

	/**
	 * TODO document me
	 */
	private Dispatcher dispatcher;
	
	/**
	 * TODO document me
	 */
	private IoAcceptor acceptor;
	
	/** The pool in which the dispatcher, acceptor and registry lives */
	private ExecutorService threadPool;

	/**
	 * TODO document me
	 */
	private ExecutorService filterchainWorkerPool;
	
	/**
	 * TODO document me
	 */
	private String protocolFactoryClassName;

	/**
	 * Creates a registry which has it's own {@link LookupTable} instead of a
	 * global.
	 * 
	 * @param port
	 *            the port the registry listens on for new connections
	 * @param threadPool
	 *            a reference to an existing thread pool
	 * @throws IOException
	 */
	protected Registry(InetAddress address, int port, ExecutorService threadPool, String protocolFactoryClassName) throws IOException {
		logger.debug("begin");
		this.lookupTableServer = new LookupTable();
		this.address  = address;
		this.port = port;
		this.threadPool = threadPool;
		this.protocolFactoryClassName = protocolFactoryClassName;
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
		
			
		dispatcher = new Dispatcher(null, lookupTableServer, threadPool);
		logger.debug("dispatcher created");
		
		acceptor = new NioSocketAcceptor();
		
		if (acceptor instanceof NioSocketAcceptor) {
			NioSocketAcceptor nioSocketAcceptor = (NioSocketAcceptor) acceptor;
			
			logger.debug("setting 'TcpNoDelay' on NioSocketAcceptor");
			nioSocketAcceptor.getSessionConfig().setTcpNoDelay(true);

			logger.debug("setting 'ReuseAddress' on NioSocketAcceptor");
			nioSocketAcceptor.setReuseAddress(true);
		}
		
//		filterchainWorkerPool = Executors.newCachedThreadPool(new NamedThreadPoolFactory(Statics.FILTERCHAIN_WORKERPOOL_NAME));
		filterchainWorkerPool = new OrderedThreadPoolExecutor();

		acceptor.getFilterChain().addFirst("executor", new ExecutorFilter(filterchainWorkerPool));
		
		// only add the logging filter if trace is enabled
		if (logger.isTraceEnabled())
        	acceptor.getFilterChain().addLast( "logger", new LoggingFilter() );

		SimonProtocolCodecFactory protocolFactory = null;
		try {
			protocolFactory = Utils.getFactoryInstance(protocolFactoryClassName);
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
        acceptor.getSessionConfig().setIdleTime( IdleStatus.BOTH_IDLE, 10 );
        acceptor.bind(new InetSocketAddress(address, port));
        
        logger.debug("acceptor thread created and started");			
        logger.debug("end");
	}
	
	/**
	 * Stops the registry. This clears the {@link LookupTable}, stops the
	 * acceptor and the {@link Dispatcher}. After running this method, no
	 * further connection/communication is possible with this registry.
	 * 
	 */
	public void stop() {
		acceptor.unbind();
		dispatcher.shutdown();
		filterchainWorkerPool.shutdown();
		lookupTableServer.clear();
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
			if (lookupTableServer.getRemoteBinding(name)!=null) 
				throw new NameBindingException("a remote object with the name '"+name+"' is already bound to this registry. unbind() first, or alternatively rebind().");
		} catch (LookupFailedException e) {
			// nothing to do
		}
		lookupTableServer.putRemoteBinding(name, remoteObject);
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
	 */
	public void unbind(String name){
		//TODO what to do with already connected users?
		lookupTableServer.releaseRemoteBinding(name);
		Simon.unpublish(new SimonPublication(address, port, name));
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
	
}
