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
import java.util.HashMap;
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


public class Simon {
	
	protected transient static Logger _log = Logger.getLogger(Simon.class.getName());
	
	/** the registry which is created by the server and holds it's own lookup table. */
	private static Registry registry = null;
	
	/** the lookup-table that is used by the client */
	private static LookupTable lookupTableClient = new LookupTable();
	
	private static final String dispatcherThreadName = "Simon.Dispatcher";
	
	private static final HashMap<String, ClientToServerConnection> serverDispatcherRelation = new HashMap<String, ClientToServerConnection>();
	
	/*
	 * Different ThreadPool implementations
	 * Is used by "ProcessMethodInvocationRunnable"
	 */
	private static ExecutorService threadPool = null;

	private static boolean registryCreated;

	private static Statistics statistics;

	private static final String threadPoolName = "Simon.Dispatcher.WorkerPool";
	
	/**
	 * Try to load 'config/simon_logging.properties'
	 */
	static {
		
		// only debug, if DEBUG flag is enabled in Utils class
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
		_log.log(Level.INFO, "Simon lib loaded");
	}
	
	public static Statistics getStatistics() {
		if (statistics==null) {
			statistics = new Statistics();
		}
		return statistics;
	}

	/**
	 * Creates a registry (listening on all interfaces) with the scope of a global lookuptable. 
	 * <br><br>
	 * <b>Example:</b><br>
	 * You want to run two servers in one application:<br>
	 * <ul>
	 * <li>Server #1 has server object 'A'.</li>
	 * <li>Server #2 has server object 'B'.</li>
	 * </ul>
	 * If you use <i>this</i> method to create the registry, you have to use 
	 * {@link Simon#bind(String, SimonRemote)} to register remoteobject 'A' and 'B'.
	 * 
	 * Remoteobject 'A' is also lookup'able from server #2. And remoteobject 'B' is lookup'able from
	 * server #1.<br>
	 * This is what is meant by "scope of global lookuptable".
	 * 
	 * @param port the port on which SIMON listens for connections
	 * @throws UnknownHostException if no IP address for the host could be found
	 * @throws IllegalStateException if a global registry is already created
	 */
	public static void createRegistry(int port) throws UnknownHostException, IllegalStateException{
		_log.fine("begin");
		if (!registryCreated) {
			registry = new Registry(lookupTableClient, port, getThreadPool());
			registryCreated = true;
			registry.start();
		} else {
			throw new IllegalStateException("global registry already created. Cannot create a " +
					"second global registry. Please consider to use " +
					"Simon.createRegistry(InetAddress, int).");
		}
		_log.fine("end");
	}
	
	/**
	 * Stops the global registry. This cleares the {@link LookupTable}, 
	 * stopps the {@link Acceptor} and the {@link Dispatcher}.
	 * After running this method, no further connection/communication is possible. YOu have to create
	 * again a registry to run server mode again.
	 *
	 * @throws IllegalStateException if there is no global registry created which can be stopped
	 */
	public static void stopRegistry() throws IllegalStateException {
		_log.fine("begin");
		if (registryCreated)
			registry.stop();
		else
			throw new IllegalStateException("cannot stop a not started registry");
		_log.fine("end");
	}
	
	/**
	 * Creates a registry with the scope of an own lookup table. 
	 *  
	 * @param address
	 * @param port
	 * @return
	 */
	public static Registry createRegistry(InetAddress address, int port){
		_log.fine("begin");
		Registry registry = new Registry(address, port, getThreadPool());
		registry.start();
		_log.fine("end");
		return registry;
	}
	
	/**
	 * 
	 * TODO: Documentation to be done for method 'lookup', by 'achristian'..
	 * 
	 * @param host
	 * @param port
	 * @param remoteObjectName
	 * @return
	 * @throws SimonRemoteException
	 * @throws IOException
	 * @throws EstablishConnectionFailed
	 */
	public static SimonRemote lookup(String host, int port, String remoteObjectName) throws SimonRemoteException, EstablishConnectionFailed {
		_log.fine("begin");
		
		// check if there is already an dispatcher and key for THIS server
		SimonRemote proxy = null;
		Dispatcher dispatcher = null;
		SelectionKey key = null;
		
		String serverString = createServerString(host, port);
		
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
					dispatcher = new Dispatcher(serverString, lookupTableClient, getThreadPool());
				} catch (IOException e) {
					throw new EstablishConnectionFailed(e.getMessage());
				}
				
				Thread clientDispatcherThread = new Thread(dispatcher,dispatcherThreadName);
				clientDispatcherThread.start();
				
				Client client = new Client(dispatcher);
				client.connect(host, port);
				_log.finer("connected with server: host="+host+" port="+port+" remoteObjectName="+remoteObjectName);
				key = client.getKey();
				
				// store this connection
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
		    
		} catch (LookupFailedException e) {
			throw new LookupFailedException(e.getMessage());
		} catch (IOException e){
			throw new EstablishConnectionFailed(e.getMessage());
		}
		
		_log.fine("end");
		return proxy;
	}

	/**
	 * 
	 * Creates a unique string for a server by using the host and port 
	 * 
	 * @param host ther servers host
	 * @param port the port the server listens on
	 * @return a server string
	 */
	private static String createServerString(String host, int port) {
		return host+":"+port;
	}
	
	/**
	 * Binds an remote object to the global registry
	 * 
	 * @param name a name for object to bind
	 * @param remoteObject the object to bind
	 */
	public static void bind(String name, SimonRemote remoteObject) {
		lookupTableClient.putRemoteBinding(name, remoteObject);
	}
	
	/**
	 * Unbinds a already bind object from the global registry.
	 *  
	 * @param name the object to unbind
	 */
	public static void unbind(String name){
		//TODO what to do with already connected users?
		lookupTableClient.releaseRemoteBinding(name);
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
	 * Retrieves {@link SimonProxy} invocationhandler wrapped in a simple proxy
	 * 
	 * @param o the object that holds the proxy
	 * @return the extracted SimonProxy
	 * @throws IllegalArgumentException if the object does not contain a SimonProxy invocationhandler
	 */
	private static SimonProxy getSimonProxy(Object o) throws IllegalArgumentException {
		if (o instanceof Proxy) {
			InvocationHandler invocationHandler = Proxy.getInvocationHandler(o);
			if (invocationHandler instanceof SimonProxy){
				return (SimonProxy) invocationHandler;
			} else throw new IllegalArgumentException("the proxys invocationhandler is not an instance of SimonProxy");
		} else throw new IllegalArgumentException("the argument is not an instance of java.lang.reflect.Proxy");
	}

	/**
	 * 
	 * Sets the number of times a remote-invocation has to be called until the object-cache is cleared.<br>
	 * This is only client-related and will be ignored by the server. So the value can differ from client to client.
	 * 
	 * @param value the int value to set
	 * @throws IllegalArgumentException if objectCacheLifetime is <1
	 * @deprecated this method doesn't have any effect on the use of simon
	 */
	public static void setObjectCacheLifetime(int value) throws IllegalArgumentException{
	}
	
	/**
	 * Gets the number of times a remote-invocation has to be called until the object-cache is cleared.
	 * 
	 * @return int value 
	 * @deprecated this will from now on return -1 ...!!!
	 */
	public static int getObjectCacheLifetime(){
		return -1;
	}

	/**
	 * Returns the reference to the worker thread pool
	 * @return the threadPool
	 */
	protected static ExecutorService getThreadPool() {
		if (threadPool==null){
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

		if (threadPool!=null) throw new IllegalStateException("You have to set the size BEFORE using createRegistry() or lookup()...");
		
		if (size==-1){
			threadPool = Executors.newCachedThreadPool(new NamedThreadPoolFactory(threadPoolName));
		} else if (size==1) {
			threadPool = Executors.newSingleThreadExecutor(new NamedThreadPoolFactory(threadPoolName));			
		} else {
			threadPool = Executors.newFixedThreadPool(size, new NamedThreadPoolFactory(threadPoolName));
		}
	}
	
	/**
	 * 
	 * Releases a instance of a remote object.
	 * If there are no more remote objects alove which are related to a specific serverconnection,
	 * the connection will be closed, until a new {@link Simon#lookup(String, int, String)} is 
	 * called on the same server.
	 * 
	 * @param proxyObject the object to release
	 * @return true if the serverconnection is closed, false if there's still a reference pending 
	 */
	public static boolean release(Object proxyObject) {
		_log.fine("begin");
		
		boolean result = false;
		// retrieve the proxyobject 
		SimonProxy proxy = getSimonProxy(proxyObject);
		
		_log.fine("releasing "+proxy);
		
		// release the proxy and get the related dispatcher
		Dispatcher dispatcher = proxy.release();
		
		// get the serverstring the dispatcher is connected to
		String serverString = dispatcher.getServerString();
		synchronized (serverDispatcherRelation) {
			
			// if there's an instance of this connection known ...
			if (serverDispatcherRelation.containsKey(serverString)) {
				
				// ... remove the connection from the list ...
				ClientToServerConnection ctsc = serverDispatcherRelation.remove(serverString);
				int refCount = ctsc.delRef();
				
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
		_log.fine("end");
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
	

}
