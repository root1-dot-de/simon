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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
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
		
	/*
	 * Different ThreadPool implementations
	 * Is used by "ProcessMethodInvocationRunnable"
	 */
	private static ExecutorService threadPool = null;

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
			
			try {
				
				is = new FileInputStream("config/simon_logging.properties");
				LogManager.getLogManager().readConfiguration(is);
				
			} catch (FileNotFoundException e) {
				
				System.err.println("File not fount: config/logging.properties.\n" +
						"If you don't want to debug SIMON, leave 'Utils.DEBUG' with false-value.\n" +
						"Otherwise you have to provide a Java Logging API conform properties-file like mentioned.");
				
			} catch (SecurityException e) {
				
				System.err.println("Security exception occured while trying to load config/logging.properties\n" +
						"Logging with SIMON not possible!.");
				
			} catch (IOException e) {
				
				System.err.println("Cannot load config/logging.properties ...\n" +
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
	 * @param host hostaddress where the lookup takes place
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
		SelectionKey key = null;
		
		String serverString = createServerString(host, port);
		
		_log.finer("check if serverstring '"+serverString+"' is already in the serverDispatcherRelation list");
		
		synchronized (serverDispatcherRelation) {
			
			if (serverDispatcherRelation.containsKey(serverString)){
				
				// retrieve the already stored connection
				ClientToServerConnection ctsc = serverDispatcherRelation.remove(serverString);
				ctsc.addRef();
				serverDispatcherRelation.put(serverString, ctsc);
				dispatcher = ctsc.getDispatcher();
				key = ctsc.getKey();
				
				_log.fine("Got ClientToServerConnection from list");
				
				
			} else {
				
				_log.fine("No ClientToServerConnection in list. Creating new one.");
				
				try {
					
//					dispatcher = new Dispatcher(serverString, lookupTableGlobal, getThreadPool());
					dispatcher = new Dispatcher(serverString, new LookupTable(), getThreadPool());
					
				} catch (IOException e) {
					
					if (dispatcher!=null) {
						_log.finest("Dispatcher creating failed, call shutdown() ...");
						dispatcher.shutdown();
					}
					// forward the exception
					throw new EstablishConnectionFailed(e.getMessage());
					
				}
				
				Thread clientDispatcherThread = new Thread(dispatcher,Statics.CLIENT_DISPATCHER_THREAD_NAME);
				clientDispatcherThread.start();
				
				Client client = new Client(dispatcher);
				try {
					
					client.connect(host, port);
					
				} catch (Exception e){
					_log.finest("Connection to server failed, call shutdown() on Dispatcher");
					dispatcher.shutdown();
					// forward exception
					throw new EstablishConnectionFailed(e.getMessage());
					
				}
				
				if (_log.isLoggable(Level.FINER)) {
					_log.finer("connected with server: host="+host+" port="+port+" remoteObjectName="+remoteObjectName);
				}
				
				key = client.getKey();
				
				// store this connection for later re-use
				ClientToServerConnection ctsc = new ClientToServerConnection(serverString,dispatcher,key);
				ctsc.addRef();
				serverDispatcherRelation.put(serverString, ctsc);
				
			}
			
		}
		
		try {
			
			
			/*
			 * Create array with interfaces the proxy should have
			 * first contact server for lookup of interfaces
			 * this request blocks!
			 */
			Class<?>[] listenerInterfaces = (Class<?>[]) dispatcher.invokeLookup(key, remoteObjectName);

			/*
			 * Creates proxy for method-call-forwarding to server 
			 */
			SimonProxy handler = new SimonProxy(dispatcher, key, remoteObjectName);
			_log.finer("proxy created");
			
			 /* 
		     * Create the proxy-object with the needed interfaces
		     */
		    proxy = (SimonRemote) Proxy.newProxyInstance(SimonClassLoader.getClassLoader(Simon.class), listenerInterfaces, handler);
		    
		} catch (IOException e){
			dispatcher.shutdown();
			throw new EstablishConnectionFailed(e.getMessage());
		}
		
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
	 * Gets the socket-inetaddress used on the remote-side of the given proxy object
	 * 
	 * @param proxyObject the proxy-object
	 * @return the InetAddress on the remote-side
	 */
	public static InetAddress getRemoteInetAddress(Object proxyObject) throws IllegalArgumentException {
		return getSimonProxy(proxyObject).getInetAddress();
	}
	
	/**
	 * 
	 * Gets the socket-port used on the remote-side of the given proxy object
	 * 
	 * @param proxyObject the proxy-object
	 * @return the port on the remote-side
	 */
	public static int getRemotePort(Object proxyObject) throws IllegalArgumentException {
		return getSimonProxy(proxyObject).getRemotePort();
	}
	
	/**
	 * 
	 * Gets the socket-port used on the local-side of the given proxy object
	 * 
	 * @param proxyObject the proxy-object
	 * @return the port on the local-side
	 */
	public static int getLocalPort(Object proxyObject) throws IllegalArgumentException {
		return getSimonProxy(proxyObject).getLocalPort();
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
		if (threadPool==null || threadPool.isShutdown()){
			
			if (poolSize!=-1)
				setWorkerThreadPoolSize(poolSize);
			else
				setWorkerThreadPoolSize(-1);
		}
		return threadPool;
	}

	/**
	 * Sets the size of the worker thread pool.<br>
	 * If given size has value -1, the pool will create new threads as needed, 
	 * but will reuse previously constructed threads when they are available. 
	 * Old, for 60 seconds unused threads will be removed. These pools will 
	 * typically improve the performance of programs that execute many short-lived 
	 * asynchronous tasks. See documentation of {@link Executors#newCachedThreadPool()}<br>
	 * 
	 * If size has value >=1, the pool has a fixed size by the given value
	 * 
	 * @param size the size of the used worker thread pool
	 */
	public static void setWorkerThreadPoolSize(int size) {

		if (threadPool!=null && !threadPool.isShutdown()) throw new IllegalStateException("You have to set the size BEFORE using createRegistry() or lookup()...");
		
		if (size==-1){
			threadPool = Executors.newCachedThreadPool(new NamedThreadPoolFactory(Statics.DISPATCHER_WORKERPOOL_NAME));
		} else if (size==1) {
			threadPool = Executors.newSingleThreadExecutor(new NamedThreadPoolFactory(Statics.DISPATCHER_WORKERPOOL_NAME));			
		} else {
			threadPool = Executors.newFixedThreadPool(size, new NamedThreadPoolFactory(Statics.DISPATCHER_WORKERPOOL_NAME));
		}
		
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

	protected static void publish(SimonPublication simonPublication) throws IOException {
		if (publishments.isEmpty()){
			publishService = new PublishService(publishments);
			publishService.start();
		}
		publishments.add(simonPublication);
		
	}

	public static void unpublish(SimonPublication simonPublication) {
		publishments.remove(simonPublication);
		if (publishments.isEmpty()) {
			publishService.shutdown();
		}
	}
	
	public static PublicationSearcher searchRemoteObjects(SearchProgressListener listener, int searchTime){
		if (publicationSearcher==null || !publicationSearcher.isSearching()) {
			try {
				publicationSearcher = new PublicationSearcher(listener, searchTime);
				publicationSearcher.start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else throw new IllegalStateException("search currently in progress ...");
		return publicationSearcher;
	}

}
