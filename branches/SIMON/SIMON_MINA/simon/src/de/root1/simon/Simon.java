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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.LogManager;

import org.apache.mina.core.future.CloseFuture;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.root1.simon.codec.base.SimonProtocolCodecFactory;
import de.root1.simon.codec.messages.MsgLookupReturn;
import de.root1.simon.exceptions.EstablishConnectionFailed;
import de.root1.simon.exceptions.LookupFailedException;
import de.root1.simon.exceptions.SimonRemoteException;
import de.root1.simon.utils.SimonClassLoader;
import de.root1.simon.utils.Utils;

/**
 * This is SIMONs core class which contains all the core functionality like
 * setting up a SIMON server or lookup a remote object from the client side
 */
public class Simon {
	
	/**
	 * TODO document me
	 */
	private final static Logger logger = LoggerFactory.getLogger(Simon.class);
	
	/**
	 * A relation map between remote object names and the SelectionKey + Dispatcher
	 */
	private static final HashMap<String, ClientToServerConnection> serverDispatcherRelation = new HashMap<String, ClientToServerConnection>();
	
	/**
	 * TODO document me
	 */
	private static int poolSize = -1;

	/**
	 * TODO document me
	 */
	private static List<SimonPublication> publishments = new ArrayList<SimonPublication>();

	/**
	 * TODO document me
	 */
	private static PublishService publishService;

	/**
	 * TODO document me
	 */
	private static PublicationSearcher publicationSearcher;
	
	/**
	 * Identifies the class, that is used as SIMON's standard protocol codec factory
	 */
	public static final String SIMON_STD_PROTOCOL_CODEC_FACTORY = "de.root1.simon.codec.base.SimonProtocolCodecFactory";

	/**
	 * TODO document me
	 */
	private static String protocolFactoryClassName = SIMON_STD_PROTOCOL_CODEC_FACTORY;

	/**
	 * Try to load 'config/simon_logging.properties'
	 */
	static {
		
		// only debug, if public DEBUG flag is enabled in Utils class
		if (Utils.DEBUG) {
			
			InputStream is;
			File f = new File("config/simon_logging.properties");
			try {
				is = new FileInputStream(f);
				LogManager.getLogManager().readConfiguration(is);
				
				logger.debug("Logging: Loaded config {}",f.getAbsolutePath());
				
			} catch (FileNotFoundException e) {
				
				System.err.println("File not fount: "+f.getAbsolutePath()+".\n" +
						"If you don't want to debug SIMON, leave 'Utils.DEBUG' with false-value.\n" +
						"Otherwise you have to provide a Java Logging API conform properties-file like mentioned.");
				
			} catch (SecurityException e) {
				
				System.err.println("Security exception occured while trying to load "+f.getAbsolutePath()+"\n" +
						"Logging with SIMON not possible!.");
				
			} catch (IOException e) {
				
				System.err.println("Cannot load "+f.getAbsolutePath()+" ...\n" +
						"Please make sure that Java has access to that file.");
				
			}
			
		}
		logger.debug("Simon lib loaded [version={}|rev={}|timestamp={}]", new Object[]{Statics.SIMON_VERSION, Statics.SIMON_BUILD_REVISION, Statics.SIMON_BUILD_TIMESTAMP});
		
	}
	
	/**
	 * Creates a registry listening on all interfaces with the last known worker
	 * thread pool size set by {@link Simon#setWorkerThreadPoolSize}
	 * 
	 * @param port
	 *            the port on which SIMON listens for connections
	 * @throws UnknownHostException
	 *             if no IP address for the host could be found
	 * @throws IOException
	 *             if there is a problem with the networking layer
	 */
	public static Registry createRegistry(int port) throws UnknownHostException, IOException{
		return createRegistry(InetAddress.getByName("0.0.0.0"),port);
	}
	
	/**
	 * Stops the given registry. This clears the {@link LookupTable} and stops
	 * the {@link Dispatcher}. After running this method, no further
	 * connection/communication is possible. You have to create again a registry
	 * to run server mode again.
	 * 
	 * @param registry
	 *            the registry to shut down
	 * 
	 * @deprecated You should call <code>stop()</code> on the registry to
	 *             shutdown the registry instead of this method.
	 */
	public static void shutdownRegistry(Registry registry) throws IllegalStateException {
		if (registry.isRunning()) 
			registry.stop();
	}
	
	/**
	 * Creates a registry listening on a specific network interface, 
	 * identified by the given {@link InetAddress} with the last known 
	 * worker thread pool size set by {@link Simon#setWorkerThreadPoolSize} 
	 *  
	 * @param address the {@link InetAddress} the registry is bind to
	 * @param port the port the registry is bind to
	 * @return the created registry
	 * @throws IOException if there is a problem with the networking layer
	 */
	public static Registry createRegistry(InetAddress address, int port) throws IOException {
		logger.debug("begin");
		Registry registry = new Registry(address, port, getThreadPool(), protocolFactoryClassName);
		logger.debug("end");
		return registry;
	}
	
	/**
	 * 
	 * Retrieves a remote object from the server. At least, it tries to retrieve
	 * it. This may fail if the named object is not available or if the
	 * connection could not be established.<br>
	 * <i>Note: If your are finished with the remote object, don't forget to
	 * call {@link Simon#release(Object)} to decrease the reference count and
	 * finally release the connection to the server</i>
	 * 
	 * @param host
	 *            hostname where the lookup takes place
	 * @param port
	 *            port number of the simon remote registry
	 * @param remoteObjectName
	 *            name of the remote object which is bind to the remote registry
	 * @return and instance of the remote object
	 * @throws SimonRemoteException
	 *             if there's a problem with the simon communication
	 * @throws IOException
	 *             if there is a problem with the communication itself
	 * @throws EstablishConnectionFailed
	 *             if its not possible to establish a connection to the remote
	 *             registry
	 * @throws LookupFailedException
	 *             if there's no such object on the server
	 */
	public static SimonRemote lookup(String host, int port, String remoteObjectName) throws SimonRemoteException, IOException, EstablishConnectionFailed, LookupFailedException {
		return lookup(InetAddress.getByName(host), port, remoteObjectName);
	}
	
	/**
	 * 
	 * Retrieves a remote object from the server. At least, it tries to retrieve
	 * it. This may fail if the named object is not available or if the
	 * connection could not be established.<br>
	 * <i>Note: If your are finished with the remote object, don't forget to
	 * call {@link Simon#release(Object)} to decrease the reference count and
	 * finally release the connection to the server</i>
	 * 
	 * @param host
	 *            host address where the lookup takes place
	 * @param port
	 *            port number of the simon remote registry
	 * @param remoteObjectName
	 *            name of the remote object which is bind to the remote registry
	 * @return and instance of the remote object
	 * @throws SimonRemoteException
	 *             if there's a problem with the simon communication
	 * @throws IOException
	 *             if there is a problem with the communication itself
	 * @throws EstablishConnectionFailed
	 *             if its not possible to establish a connection to the remote
	 *             registry
	 * @throws LookupFailedException
	 *             if there's no such object on the server
	 */
	public static SimonRemote lookup(InetAddress host, int port, String remoteObjectName) throws LookupFailedException, SimonRemoteException, IOException, EstablishConnectionFailed {
		logger.debug("begin");
		
		// check if there is already an dispatcher and key for THIS server
		SimonRemote proxy = null;
		Dispatcher dispatcher = null;
		IoSession session = null;
		
		String serverString = createServerString(host, port);
		
		logger.debug("check if serverstring '{}' is already in the serverDispatcherRelation list", serverString);
		
		synchronized (serverDispatcherRelation) {
			
			if (serverDispatcherRelation.containsKey(serverString)){
				
				// retrieve the already stored connection
				ClientToServerConnection ctsc = serverDispatcherRelation.remove(serverString);
				ctsc.addRef();
				serverDispatcherRelation.put(serverString, ctsc);
				dispatcher = ctsc.getDispatcher();
				session = ctsc.getSession();
				
				logger.debug("Got ClientToServerConnection from list");
				
				
			} else {
				
				logger.debug("No ClientToServerConnection in list. Creating new one.");
				
				dispatcher = new Dispatcher(serverString, new LookupTable(), getThreadPool());
				ExecutorService filterchainWorkerPool = Executors.newCachedThreadPool(new NamedThreadPoolFactory(Statics.FILTERCHAIN_WORKERPOOL_NAME));
				
				IoConnector connector = new NioSocketConnector();
				
				connector.setHandler(dispatcher);
								
				ConnectFuture future = connector.connect(new InetSocketAddress(host, port));
				future.awaitUninterruptibly(); // Wait until the connection attempt is finished.
				session = future.getSession();
				
				session.getFilterChain().addFirst("executor", new ExecutorFilter(filterchainWorkerPool));
				
				if (logger.isTraceEnabled())
					session.getFilterChain().addLast( "logger", new LoggingFilter() );
				
				SimonProtocolCodecFactory protocolFactory = null;
				try {
					protocolFactory = Utils.getFactoryInstance(protocolFactoryClassName);
				} catch (ClassNotFoundException e) {
					// already proved
					logger.warn("this should never happen. Please contact author. -> {}", e.getMessage());
				} catch (InstantiationException e) {
					// already proved
					logger.warn("this should never happen. Please contact author. -> {}", e.getMessage());
				} catch (IllegalAccessException e) {
					// already proved
					logger.warn("this should never happen. Please contact author. -> {}", e.getMessage());
				}
				protocolFactory.setup(false);
				
				session.getFilterChain().addLast("codec", new ProtocolCodecFilter(protocolFactory));
				
				logger.trace("connected with server: host={} port={} remoteObjectName={}", new Object[]{host, port, remoteObjectName});
				
				// store this connection for later re-use
				ClientToServerConnection ctsc = new ClientToServerConnection(serverString,dispatcher,session, connector, filterchainWorkerPool);
				ctsc.addRef();
				serverDispatcherRelation.put(serverString, ctsc);
				
			}
			
		}
			
		/*
		 * Create array with interfaces the proxy should have
		 * first contact server for lookup of interfaces
		 * this request blocks!
		 */
		MsgLookupReturn msg = dispatcher.invokeLookup(session, remoteObjectName);
		Class<?>[] listenerInterfaces = msg.getInterfaces();

		/*
		 * Creates proxy for method-call-forwarding to server 
		 */
		SimonProxy handler = new SimonProxy(dispatcher, session, remoteObjectName);
		logger.trace("proxy created");
		
		 /* 
	     * Create the proxy-object with the needed interfaces
	     */
	    proxy = (SimonRemote) Proxy.newProxyInstance(SimonClassLoader.getClassLoader(Simon.class), listenerInterfaces, handler);
		
		logger.debug("end");
		return proxy;
	}

	/**
	 * 
	 * Creates a unique string for a server by using the host and port
	 * 
	 * @param host
	 *            the servers host
	 * @param port
	 *            the port the server listens on
	 * @return a server string
	 */
	private static String createServerString(InetAddress host, int port) {
		return host.getHostAddress()+":"+port;
	}
	
	/**
	 * 
	 * Gets the InetSocketAddress used on the remote-side of the given proxy
	 * object
	 * 
	 * @param proxyObject
	 *            the proxy object
	 * @return the InetSocketAddress on the remote-side
	 */
	public static InetSocketAddress getRemoteInetSocketAddress(Object proxyObject) throws IllegalArgumentException {
		return (InetSocketAddress) getSimonProxy(proxyObject).getRemoteSocketAddress();
	}
	
	/**
	 * 
	 * Gets the socket-inetaddress used on the remote-side of the given proxy
	 * object
	 * 
	 * @param proxyObject
	 *            the proxy-object
	 * @return the InetAddress on the remote-side
	 * @deprecated use
	 *             <code>Simon.getRemoteInetSocketAddress(Object).getAddress()</code>
	 *             instead!
	 */
	public static InetAddress getRemoteInetAddress(Object proxyObject) throws IllegalArgumentException {
		return getRemoteInetSocketAddress(proxyObject).getAddress();
	}
	
	/**
	 * 
	 * Gets the socket-port used on the remote-side of the given proxy object
	 * 
	 * @param proxyObject
	 *            the proxy-object
	 * @return the port on the remote-side
	 * @deprecated use
	 *             <code> Simon.getRemoteInetSocketAddress(proxyObject).getPort()</code>
	 *             instead!
	 */
	public static int getRemotePort(Object proxyObject) throws IllegalArgumentException {
		return getRemoteInetSocketAddress(proxyObject).getPort();
	}
	
	/**
	 * 
	 * Gets the InetSocketAddress used on the local-side of the given proxy
	 * object
	 * 
	 * @param proxyObject
	 *            the proxy object
	 * @return the InetSocketAddress on the local-side
	 */
	public static InetSocketAddress getLocalInetSocketAddress(Object proxyObject) throws IllegalArgumentException {
		return (InetSocketAddress) getSimonProxy(proxyObject).getLocalSocketAddress();
	}
	
	/**
	 * 
	 * Gets the socket-port used on the local-side of the given proxy object
	 * 
	 * @param proxyObject
	 *            the proxy-object
	 * @return the port on the local-side
	 * @deprecated use
	 *             <code>Simon.getLocalInetSocketAddress(proxyObject).getPort()</code>
	 *             instead!
	 *             
	 */
	public static int getLocalPort(Object proxyObject) throws IllegalArgumentException {
		return getLocalInetSocketAddress(proxyObject).getPort();
	}
	
	/**
	 * 
	 * Retrieves {@link SimonProxy} invocation handler wrapped in a simple proxy
	 * 
	 * @param o
	 *            the object that holds the proxy
	 * @return the extracted SimonProxy
	 * @throws IllegalArgumentException
	 *             if the object does not contain a SimonProxy invocation
	 *             handler
	 */
	protected static SimonProxy getSimonProxy(Object o) throws IllegalArgumentException {
		if (o instanceof Proxy) {
			InvocationHandler invocationHandler = Proxy.getInvocationHandler(o);
			if (invocationHandler instanceof SimonProxy){
				return (SimonProxy) invocationHandler;
			} else throw new IllegalArgumentException("the proxys invocationhandler is not an instance of SimonProxy");
		} else throw new IllegalArgumentException("the argument is not an instance of java.lang.reflect.Proxy");
	}

	/**
	 * Returns the reference to the worker thread pool
	 * 
	 * @return the threadPool
	 */
	protected static ExecutorService getThreadPool() {
		if (poolSize==-1){
			return Executors.newCachedThreadPool(new NamedThreadPoolFactory(Statics.DISPATCHER_WORKERPOOL_NAME));
		} else if (poolSize==1) {
			return Executors.newSingleThreadExecutor(new NamedThreadPoolFactory(Statics.DISPATCHER_WORKERPOOL_NAME));			
		} else {
			return Executors.newFixedThreadPool(poolSize, new NamedThreadPoolFactory(Statics.DISPATCHER_WORKERPOOL_NAME));
		}
	}

	/**
	 * Sets the size of the worker thread pool.<br>
	 * This will setting only affect new pool that have to be created in future.
	 * If given size has value -1, a new pool will create new threads as needed,
	 * but will reuse previously constructed threads when they are available.
	 * This is the most common setting. Old, for 60 seconds unused threads will
	 * be removed. These pools will typically improve the performance of
	 * programs that execute many short-lived asynchronous tasks. See
	 * documentation of {@link Executors#newCachedThreadPool()}<br>
	 * 
	 * If size has value >=1, a new pool has a fixed size by the given value
	 * 
	 * @param size
	 *            the size of the used worker thread pool
	 */
	public static void setWorkerThreadPoolSize(int size) {
		poolSize = size;
	}
	
	/**
	 * 
	 * Releases an instance of a remote object.<br>
	 * If there are no more remote objects alive which are related to a specific
	 * server connection, this connection will be closed, until a new
	 * {@link Simon#lookup(String, int, String)} is called on the same server.
	 * 
	 * @param proxyObject
	 *            the object to release
	 * @return true if the server connection is closed, false if there's still a
	 *         reference pending
	 */
	public static boolean release(Object proxyObject) {
		logger.debug("begin");
		
		// retrieve the proxy object 
		SimonProxy proxy = getSimonProxy(proxyObject);
		
		logger.debug("releasing proxy {}",proxy.getDetailString());
		
		// release the proxy and get the related dispatcher
		Dispatcher dispatcher = proxy.release();
		
		// get the serverstring the dispatcher is connected to
		String serverString = dispatcher.getServerString();
		boolean result = releaseServerDispatcherRelation(serverString);
		
		logger.debug("end");
		return result;
	}

	/**
	 * 
	 * Releases a reference for a {@link Dispatcher} identified by a specific
	 * server string (see: {@link Simon#createServerString}. If there is no more
	 * server string referencing the Dispatcher, the Dispatcher will be
	 * released/shutdown.
	 * 
	 * @param serverString
	 *            the identifier of the Dispatcher to release
	 * @return true if the Dispatcher is shut down, false if there's still a
	 *         reference pending
	 */
	protected static boolean releaseServerDispatcherRelation(String serverString) {
		
		boolean result = false;

		synchronized (serverDispatcherRelation) {
			
			// if there's an instance of this connection known ...
			if (serverDispatcherRelation.containsKey(serverString)) {
				
				// ... remove the connection from the list ...
				final ClientToServerConnection ctsc = serverDispatcherRelation.remove(serverString);
				int refCount = ctsc.delRef();
				
				logger.trace("removed serverString '{}' from serverDispatcherRelation. new refcount is {}",serverString, refCount);
				
				if (refCount==0) {
					// .. and shutdown the dispatcher if there's no further reference
					logger.debug("refCount reached 0. shutting down session and all related stuff.");
					ctsc.getDispatcher().shutdown();
					CloseFuture closeFuture = ctsc.getSession().close(false);
					
					closeFuture.addListener(new IoFutureListener<IoFuture>(){

						public void operationComplete(IoFuture future) {
							ctsc.getFilterchainWorkerPool().shutdown();
							ctsc.getConnector().dispose();
						}
						
					});
					result = true;
				} else {
					logger.debug("refCount={}. put back the ClientToServerConnection.",refCount);
					serverDispatcherRelation.put(serverString, ctsc);
				}
				
			}
		
		}
		return result;
	}
	
	/**
	 * Sets the DGC's interval time in milliseconds
	 * 
	 * @param milliseconds
	 *            time in milliseconds
	 * 
	 * @deprecated This is now handled by MINA. Using this method is obsolete.
	 */
	public static void setDgcInterval(int milliseconds){
	}
	
	/**
	 * Gets the DGC's interval time in milliseconds return DGC interval time in
	 * milliseconds
	 * 
	 * @return the current set DGC interval
	 * 
	 * @deprecated this is now done internally with MINA. There's no global
	 *             value available...This method now always returns zero. So do
	 *             not use it anymore!
	 */
	public static int getDgcInterval(){
		return 0;
	}

	/**
	 * Publishes a remote object. If not already done, publish service thread is
	 * started.
	 * 
	 * @param simonPublication
	 *            the object to publish
	 * @throws IOException
	 *             if the publish service cannot be started due to IO problems
	 */
	protected static void publish(SimonPublication simonPublication) throws IOException {
		if (publishments.isEmpty()){
			publishService = new PublishService(publishments);
			publishService.start();
		}
		publishments.add(simonPublication);
		
	}

	/**
	 * Unpublishs a already published {@link SimonPublication}. If there are no
	 * more publications available, shutdown the publish service.
	 * 
	 * @param simonPublication
	 *            the publication to unpublish
	 */
	public static void unpublish(SimonPublication simonPublication) {
		publishments.remove(simonPublication);
		if (publishments.isEmpty() && publishService!=null && publishService.isAlive()) {
			publishService.shutdown();
		}
	}
	
	/**
	 * Creates a background thread that searches for published remote objects on
	 * the local network
	 * 
	 * @param listener
	 *            a {@link SearchProgressListener} implementation which is
	 *            informed about the current search progress
	 * @param searchTime
	 *            the time the background search thread spends for searching
	 *            published remote objects
	 * @return a {@link PublicationSearcher} which is used to access the search
	 *         result
	 */
	public static PublicationSearcher searchRemoteObjects(SearchProgressListener listener, int searchTime){
		if (publicationSearcher==null || !publicationSearcher.isSearching()) {
			try {
				publicationSearcher = new PublicationSearcher(listener, searchTime);
				publicationSearcher.start();
			} catch (IOException e) {
				// TODO what to do?
				e.printStackTrace();
			}
		} else throw new IllegalStateException("another search is currently in progress ...");
		return publicationSearcher;
	}
	
	/**
	 * Starts a search on the local network for published remote objects. <br>
	 * <b><u>Be warned:</u> This method blocks until the search is finished or
	 * the current thread is interrupted</b>
	 * 
	 * @param searchTime
	 *            the time that is spend to search for published remote objects
	 * @return a {@link List} of {@link SimonPublication}s
	 */
	public static List<SimonPublication> searchRemoteObjects(int searchTime){
		if (publicationSearcher==null || !publicationSearcher.isSearching()) {
			
			try {
				publicationSearcher = new PublicationSearcher(null, searchTime);
				publicationSearcher.run(); // call run without starting the thread. call is synchronously!
			} catch (IOException e) {
				// TODO what to do?
				e.printStackTrace();
				return null;
			} 
			
			return publicationSearcher.getNewPublications();
		} else throw new IllegalStateException("another search is currently in progress ...");
	}
	
	/**
	 * Sets class name for the protocol codec factory to use for all future
	 * <code>createRegistry()</code> or <code>lookup()</code> calls. <i>This
	 * does not affect already created registry or already established
	 * sessions.</i>
	 * 
	 * @param protocolFactoryClassName
	 *            a class name like
	 *            "com.mydomain.myproject.codec.mySimonProtocolCodecFactory"
	 *            which points to a class, that extends
	 *            {@link SimonProtocolCodecFactory}. <i>The important thing is,
	 *            that this class correctly overrides
	 *            {@link SimonProtocolCodecFactory#setup(boolean)}. For further
	 *            details, look at {@link SimonProtocolCodecFactory}!</i>
	 * @throws IllegalAccessException
	 *             if the class or its nullary constructor is not accessible.
	 * @throws InstantiationException
	 *             if this Class represents an abstract class, an interface, an
	 *             array class, a primitive type, or void; or if the class has
	 *             no nullary constructor; or if the instantiation fails for
	 *             some other reason.
	 * @throws ClassNotFoundException
	 *             if the class is not found by the classloader. if so, please
	 *             check your classpath.
	 * @throws ClassCastException
	 *             if the given class is no instance of
	 *             {@link SimonProtocolCodecFactory}
	 */
	public static void setProtocolCodecFactory(String protocolFactoryClassName) throws InstantiationException, IllegalAccessException, ClassNotFoundException, ClassCastException{
		// testwise try to get the factory. if the specified class' name is not useable, 
		// exceptions will be thrown and forwarded
		Utils.getFactoryInstance(protocolFactoryClassName);
		// if the above worked, save the class' name
		Simon.protocolFactoryClassName = protocolFactoryClassName;
	}
	
	/**
	 * Returns the current set class name for the protocol codec factory
	 * 
	 * @return the name of the protocol codec class
	 */
	public static String getProtocolCodecFactory(){
		return Simon.protocolFactoryClassName;
	}

	

}
