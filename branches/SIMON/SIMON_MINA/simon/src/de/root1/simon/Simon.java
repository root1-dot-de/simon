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
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import de.root1.simon.codec.base.SimonStdProtocolCodecFactory;
import de.root1.simon.codec.messages.MsgLookupReturn;
import de.root1.simon.exceptions.EstablishConnectionFailed;
import de.root1.simon.exceptions.LookupFailedException;
import de.root1.simon.exceptions.SimonRemoteException;
import de.root1.simon.utils.SimonClassLoader;
import de.root1.simon.utils.Utils;

/**
 * This is SIMONs core class which contains all the core functionality like setting up a SIMON server or lookup a remote object from the client side
 * 
 */
public class Simon {
	
	protected transient static Logger _log = Logger.getLogger(Simon.class.getName());
	
	/**
	 * A relation map between remote object names and the SelectionKey + Dispatcher
	 */
	private static final HashMap<String, ClientToServerConnection> serverDispatcherRelation = new HashMap<String, ClientToServerConnection>();
	
	/**
	 * A list with registries. This is used by the "automatically find servers" feature.
	 * Each registry listed here, will be published to anyone who asks.
	 */
	private static final List<Registry> registryList = new ArrayList<Registry>();
		
	private static Statistics statistics;

	private static int poolSize = -1;

	private static List<SimonPublication> publishments = new ArrayList<SimonPublication>();

	private static PublishService publishService;

	private static PublicationSearcher publicationSearcher;

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
				_log.fine("Logging: Loaded config "+f.getAbsolutePath());
				
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
		_log.log(Level.FINE, "Simon lib loaded [version="+Statics.SIMON_VERSION+"|rev="+Statics.SIMON_BUILD_REVISION+"|timestamp="+Statics.SIMON_BUILD_TIMESTAMP+"]");
	}
	
	public static Statistics getStatistics() {
		if (statistics==null) {
			statistics = new Statistics();
		}
		return statistics;
	}

	/**
	 * Creates a registry listening on all interfaces with the last known 
	 * worker thread pool size set by {@link Simon#setWorkerThreadPoolSize}
	 * 
	 * @param port the port on which SIMON listens for connections
	 * @throws UnknownHostException if no IP address for the host could be found
	 * @throws IOException if there is a problem with the networking layer
	 */
	public static Registry createRegistry(int port) throws UnknownHostException, IOException{
		
		return createRegistry(InetAddress.getByName("0.0.0.0"),port);
		
	}
	
	/**
	 * Stops the given registry. This cleares the {@link LookupTable}, 
	 * stops the {@link Acceptor} and the {@link Dispatcher}.
	 * After running this method, no further connection/communication is possible. You have to create
	 * again a registry to run server mode again.
	 *
	 * @param registry the registry to shut down
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
		_log.fine("begin");
		Registry registry = new Registry(address, port, getThreadPool());
		_log.fine("end");
		return registry;
	}
	
	/**
	 * 
	 * Retrieves a remote object from the server. At least, it tries to retrieve it.
	 * This may fail if the named object is not available or if the connection could not be established.
	 * 
	 * @param host hostname where the lookup takes place
	 * @param port port number of the simon remote registry
	 * @param remoteObjectName name of the remote object which is bind to the remote registry 
	 * @return and instance of the remote object
	 * @throws SimonRemoteException if there's a problem with the simon communication
	 * @throws IOException if there is a problem with the communication itself
	 * @throws EstablishConnectionFailed if its not possible to establish a connection to the remote registry
	 * @throws LookupFailedException if there's no such object on the server
	 */
	public static SimonRemote lookup(String host, int port, String remoteObjectName) throws SimonRemoteException, IOException, EstablishConnectionFailed, LookupFailedException {
		return lookup(InetAddress.getByName(host), port, remoteObjectName);
	}
	
	/**
	 * 
	 * Retrieves a remote object from the server. At least, it tries to retrieve it.
	 * This may fail if the named object is not available or if the connection could not be established.
	 * 
	 * @param host host address where the lookup takes place
	 * @param port port number of the simon remote registry
	 * @param remoteObjectName name of the remote object which is bind to the remote registry 
	 * @return and instance of the remote object
	 * @throws SimonRemoteException if there's a problem with the simon communication
	 * @throws IOException if there is a problem with the communication itself
	 * @throws EstablishConnectionFailed if its not possible to establish a connection to the remote registry
	 * @throws LookupFailedException if there's no such object on the server
	 */
	public static SimonRemote lookup(InetAddress host, int port, String remoteObjectName) throws LookupFailedException, SimonRemoteException, IOException, EstablishConnectionFailed, LookupFailedException {
		_log.fine("begin");
		
		// check if there is already an dispatcher and key for THIS server
		SimonRemote proxy = null;
		Dispatcher dispatcher = null;
		IoSession session = null;
		
		String serverString = createServerString(host, port);
		
		_log.finer("check if serverstring '"+serverString+"' is already in the serverDispatcherRelation list");
		
		synchronized (serverDispatcherRelation) {
			
			if (serverDispatcherRelation.containsKey(serverString)){
				
				// retrieve the already stored connection
				ClientToServerConnection ctsc = serverDispatcherRelation.remove(serverString);
				ctsc.addRef();
				serverDispatcherRelation.put(serverString, ctsc);
				dispatcher = ctsc.getDispatcher();
				session = ctsc.getSession();
				
				_log.fine("Got ClientToServerConnection from list");
				
				
			} else {
				
				_log.fine("No ClientToServerConnection in list. Creating new one.");
				
				dispatcher = new Dispatcher(serverString, new LookupTable(), getThreadPool());
				ExecutorService executorPool = Executors.newCachedThreadPool();
				NioSocketConnector connector = new NioSocketConnector();
				connector.setHandler(dispatcher);
				
				ConnectFuture future = connector.connect(new InetSocketAddress(host, port));
				future.awaitUninterruptibly(); // Wait until the connection attempt is finished.
				session = future.getSession();
				
				session.getFilterChain().addFirst("executor", new ExecutorFilter(executorPool));
				session.getFilterChain().addLast( "logger", new LoggingFilter() );
				session.getFilterChain().addLast("codec", new ProtocolCodecFilter( new SimonStdProtocolCodecFactory(false)));
				
				if (_log.isLoggable(Level.FINER)) {
					_log.finer("connected with server: host="+host+" port="+port+" remoteObjectName="+remoteObjectName);
				}
				
				// store this connection for later re-use
				ClientToServerConnection ctsc = new ClientToServerConnection(serverString,dispatcher,session);
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
		_log.finer("proxy created");
		
		 /* 
	     * Create the proxy-object with the needed interfaces
	     */
	    proxy = (SimonRemote) Proxy.newProxyInstance(SimonClassLoader.getClassLoader(Simon.class), listenerInterfaces, handler);
		    
		
		_log.fine("end");
		return proxy;
	}

	/**
	 * 
	 * Creates a unique string for a server by using the host and port 
	 * 
	 * @param host the servers host
	 * @param port the port the server listens on
	 * @return a server string
	 */
	private static String createServerString(InetAddress host, int port) {
		return host.getHostAddress()+":"+port;
	}
	
	/**
	 * 
	 * Gets the SocketAddress used on the remote-side of the given proxy object
	 * 
	 * @param proxyObject the proxy-object
	 * @return the SocketAddress on the remote-side
	 */
	public static SocketAddress getRemoteInetAddress(Object proxyObject) throws IllegalArgumentException {
		return getSimonProxy(proxyObject).getInetAddress();
	}
	
	/**
	 * 
	 * FIXME Gets the socket-port used on the remote-side of the given proxy object
	 * 
	 * @param proxyObject the proxy-object
	 * @return the port on the remote-side
	 */
	public static int getRemotePort(Object proxyObject) throws IllegalArgumentException {
		return 0;
	}
	
	/**
	 * 
	 * FIXME Gets the socket-port used on the local-side of the given proxy object
	 * 
	 * @param proxyObject the proxy-object
	 * @return the port on the local-side
	 */
	public static int getLocalPort(Object proxyObject) throws IllegalArgumentException {
		return 0;
	}
	
	/**
	 * 
	 * Retrieves {@link SimonProxy} invocation handler wrapped in a simple proxy
	 * 
	 * @param o the object that holds the proxy
	 * @return the extracted SimonProxy
	 * @throws IllegalArgumentException if the object does not contain a SimonProxy invocation handler
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
	 * Sets the size of the worker thread pool.<br>This will setting only affect new pool that have to be created in future.
	 * If given size has value -1, a new pool will create new threads as needed, 
	 * but will reuse previously constructed threads when they are available. This is the most common setting.
	 * Old, for 60 seconds unused threads will be removed. These pools will 
	 * typically improve the performance of programs that execute many short-lived 
	 * asynchronous tasks. See documentation of {@link Executors#newCachedThreadPool()}<br>
	 * 
	 * If size has value >=1, a new pool has a fixed size by the given value
	 * 
	 * @param size the size of the used worker thread pool
	 */
	public static void setWorkerThreadPoolSize(int size) {
		poolSize = size;
	}
	
	/**
	 * 
	 * Releases a instance of a remote object.
	 * If there are no more remote objects alive which are related to a specific server connection,
	 * the connection will be closed, until a new {@link Simon#lookup(String, int, String)} is 
	 * called on the same server.
	 * 
	 * @param proxyObject the object to release
	 * @return true if the serverconnection is closed, false if there's still a reference pending 
	 */
	public static boolean release(Object proxyObject) {
		_log.fine("begin");
		
		// retrieve the proxyobject 
		SimonProxy proxy = getSimonProxy(proxyObject);
		
		if (_log.isLoggable(Level.FINE))
			_log.fine("releasing "+proxy);
		
		// release the proxy and get the related dispatcher
		Dispatcher dispatcher = proxy.release();
		
		// get the serverstring the dispatcher is connected to
		String serverString = dispatcher.getServerString();
		boolean result = releaseServerDispatcherRelation(serverString);
		
		_log.fine("end");
		return result;
	}

	/**
	 * 
	 * Releases a reference for a {@link Dispatcher} identified by a specific server string (see: {@link Simon#createServerString}.
	 * If there is no more server string referencing the Dispatcher, the Dispatcher will be released/shutdown.
	 * 
	 * @param serverString the identifier of the Dispatcher to release
	 * @return true if the Dispatcher is shut down, false if there's still a reference pending 
	 */
	protected static boolean releaseServerDispatcherRelation(String serverString) {
		
		boolean result = false;

		synchronized (serverDispatcherRelation) {
			
			// if there's an instance of this connection known ...
			if (serverDispatcherRelation.containsKey(serverString)) {
				
				// ... remove the connection from the list ...
				ClientToServerConnection ctsc = serverDispatcherRelation.remove(serverString);
				int refCount = ctsc.delRef();
				
				if (_log.isLoggable(Level.FINER))
					_log.finer("removed serverString '"+serverString+"' from serverDispatcherRelation. new refcount is "+refCount);
				
				if (refCount==0) {
					// .. and shutdown the dispatcher if there's no further reference
					ctsc.getDispatcher().shutdown();
					result = true;
					_log.fine("refCount reached 0. shutting down dispatcher.");
				} else {
					_log.fine("refCount="+refCount+". put back the ClientToServerConnection.");
					serverDispatcherRelation.put(serverString, ctsc);
				}
				
			}
		
		}
		return result;
	}
	
	/**
	 * Sets the DGC's interval time in milliseconds
	 * @param milliseconds time in milliseconds
	 */
	public static void setDgcInterval(int milliseconds){
		Statics.DGC_INTERVAL = milliseconds;
	}
	
	/**
	 * Gets the DGC's interval time in milliseconds
	 * return DGC interval time in milliseconds
	 */
	public static int getDgcInterval(){
		return Statics.DGC_INTERVAL;
	}

	/**
	 * Removes a registry from SIMONs internal list of registrys.
	 * @param aRegistry the registry to remove
	 */
	protected static void removeRegistryFromList(Registry aRegistry) {
		synchronized (registryList) {
			registryList.remove(aRegistry);
		}
			
	}
	
	/**
	 * Adds a registry to SIMONs internal list of registrys.
	 * 
	 * @param aRegistry the registry to add
	 */
	protected static void addRegistryToList(Registry aRegistry) {
		synchronized (registryList) {
			registryList.add(aRegistry);
		}
	}

	/**
	 * Publishes a remote object. If not already done, start the publish service thread.
	 * 
	 * @param simonPublication the object to publish
	 * @throws IOException if the publish service cannot be started due to IO problems
	 */
	protected static void publish(SimonPublication simonPublication) throws IOException {
		if (publishments.isEmpty()){
			publishService = new PublishService(publishments);
			publishService.start();
		}
		publishments.add(simonPublication);
		
	}

	/**
	 * Unpublishs a already published {@link SimonPublication}. If there are no more
	 * publications available, shutdown the publish service.
	 * 
	 * @param simonPublication the publication to unpublish
	 */
	public static void unpublish(SimonPublication simonPublication) {
		publishments.remove(simonPublication);
		if (publishments.isEmpty() && publishService!=null && publishService.isAlive()) {
			publishService.shutdown();
		}
	}
	
	/**
	 * Creates a background thread that searches for published remote objects
	 * 
	 * @param listener a {@link SearchProgressListener} implementation which is informed about the current search progress
	 * @param searchTime the time the background search thread spends for searching published remote objects
	 * @return a {@link PublicationSearcher} which is used to access the search result
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
	 * Starts a search for published remote objects. <br>
	 * <b><u>Be warned:</u> This method blocks until the search is finished or the current thread is interrupted</b>
	 * @param searchTime the time that is spend to search for published remote objects
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

}