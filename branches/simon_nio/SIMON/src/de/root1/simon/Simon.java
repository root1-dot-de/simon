/*
 * Copyright 2008 Alexander Christian <alex(at)root1.de>. All rights reserved.
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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.root1.simon.utils.SimonClassLoader;
import de.root1.simon.utils.Utils;


public class Simon {
	
	private static Registry registry;
	private static LookupTable lookupTable = new LookupTable();
	
	/*
	 * Different ThreadPool implementations
	 * Is used by "ProcessMethodInvocationRunnable"
	 */
	private static ExecutorService threadPool = null;
	

	/**
	 * Creates a registry
	 * TODO Documentation to be done
	 * @param port
	 */
	public static void createRegistry(int port){
		registry = new Registry(lookupTable, port, getThreadPool());
		registry.start();
	}
	
	public static Object lookup(String host, int port, String remoteObjectName) throws SimonRemoteException, ConnectException {
		Utils.debug("Simon.lookup() -> START");
		Object proxy = null;
		
		
			
		try {	
			InetAddress addr = InetAddress.getByName(host);
			SocketAddress sockaddr = new InetSocketAddress(addr, port);

			// Create an unbound socket
//			Socket socket = new Socket();
			
//			preSetupSocket(socket);

			// This method will block no more than timeoutMs.
			// If the timeout occurs, SocketTimeoutException is thrown.
//			int timeoutMs = 2000; // 2 seconds
//			socket.connect(sockaddr, timeoutMs);
			
//			postSetupSocket(socket);
			
			
			Utils.debug("Simon.lookup() -> connected with server ...");
			
			// FIXME make sure endpoint is feed with all needed data
			Dispatcher endpoint = null;
//			= new Dispatcher(lookupTable, "Client", false, port);
			// FIXME should the invoke-methods should be "outside" the dispatcher ??
			new Thread(endpoint).start();
			Utils.debug("Simon.lookup() -> Endpoint thread started");
			
			
			// ab hier serverantwort auswerten
			
			/*
			 * Create array with interfaces the proxy should have
			 * first contact server for lookup of interfaces
			 * this request blocks!
			 */
			Class<?>[] listenerInterfaces = (Class<?>[]) endpoint.invokeLookup(remoteObjectName);
			
			/*
			 * This class gets the interfaces and directs the method-calls
			 */
			SimonProxy handler = new SimonProxy(endpoint, remoteObjectName);
			Utils.debug("Simon.lookup() -> Proxy created");
			
			 /* 
		     * Create the proxy-object with the needed interfaces
		     */
//		    proxy = Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), listenerInterfaces, handler);
		    proxy = Proxy.newProxyInstance(SimonClassLoader.getClassLoader(Simon.class), listenerInterfaces, handler);
		} catch (IOException e){
			throw new ConnectException(e.getMessage());
		}
		
		Utils.debug("Simon.lookup() -> END");
		return proxy;
	}

//	/**
//	 * 
//	 * Sets some post socket-specific parameters and tuning settings
//	 * 
//	 * @param socket
//	 * @throws IOException
//	 * @throws SocketException
//	 */
//	protected static void postSetupSocket(Socket socket)
//			throws IOException, SocketException {
//		/*
//		 * Disable the Nagle-algorithm. See also
//		 * http://de.wikipedia.org/wiki/Nagle-Algorithmus
//		 */
//		socket.setTcpNoDelay(true); 
//		
//		// Priority: low latency > bandwidth > connection time
//		socket.setPerformancePreferences(0, 2, 1); 
//	}

//	/**
//	 * 
//	 * Sets some pre socket-specific parameters and tuning settings
//	 * 
//	 * @param socket
//	 * @throws SocketException
//	 */
//	protected static void preSetupSocket(Socket socket) throws SocketException {
//		/*
//		 * Detect MacOS ...
//		 * See: 
//		 * Identifying Mac OS X in Java, http://developer.apple.com/technotes/tn2002/tn2110.html
//		 */
//		String lcOSName = System.getProperty("os.name").toLowerCase();
//		boolean MAC_OS_X = lcOSName.startsWith("mac os x");
//		
//		/*
//		 * MAC OS uses per default IPv6 setting which cannot handle setTrafficClass() method.
//		 * It's possible to set a system property (java.net.preferIPv4Stack" -> true) to perfer IPv4,
//		 * which is able to handle the method, but setting system properties are problematically with java applets.
//		 * According to a few sites on the web which refers to a RFC blablabla, the traffic-class is since 1998 obselete
//		 * So we just disable the method for mac os.
//		 */
//		if (!MAC_OS_X) {
//			socket.setTrafficClass(0x10); // prefer low delay			
//		}
//	}
	
	/**
	 * Binds a Object to the registry
	 * TODO Documentation to be done
	 * @param name
	 * @param remoteObject
	 */
	public static void bind(String name, SimonRemote remoteObject) {
		lookupTable.putRemoteBinding(name, remoteObject);
	}

	// FIXME reimplement asking for ip-address if client
//	/**
//	 * 
//	 * Gets the socket-inetaddress used on the remote-side of the given proxy object
//	 * 
//	 * @param proxyObject the proxy-object
//	 * @return the InetAddress on the remote-side
//	 */
//	public static InetAddress getRemoteInetAddress(Object proxyObject) throws IllegalArgumentException {
//		return getSimonProxy(proxyObject).getInetAddress();
//	}
//	
//	/**
//	 * 
//	 * Gets the socket-port used on the remote-side of the given proxy object
//	 * 
//	 * @param proxyObject the proxy-object
//	 * @return the port on the remote-side
//	 */
//	public static int getRemotePort(Object proxyObject) throws IllegalArgumentException {
//		return getSimonProxy(proxyObject).getPort();
//	}
	
//	/**
//	 * 
//	 * Checks the given objekt for a SimonProxy invocationhandler wrapped in a simple proxy
//	 * 
//	 * @param o the object to check
//	 * @return the extrected SimonProxy
//	 * @throws IllegalArgumentException if the object does not contain a SimonProxy invocationhandler
//	 */
//	private static SimonProxy getSimonProxy(Object o) throws IllegalArgumentException {
//		if (o instanceof Proxy) {
//			InvocationHandler invocationHandler = Proxy.getInvocationHandler(o);
//			if (invocationHandler instanceof SimonProxy){
//				return (SimonProxy) invocationHandler;
//			} else throw new IllegalArgumentException("the proxys invocationhandler is not an instance of SimonProxy");
//		} else throw new IllegalArgumentException("the argument is not an instance of java.lang.reflect.Proxy");
//	}

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
	 * if given size has value -1, the pool will create new threads as needed, 
	 * but will reuse previously constructed threads when they are available. 
	 * Old, for 60 seconds unused threads will be removed. These pools will 
	 * typically improve the performance of programs that execute many short-lived 
	 * asynchronous tasks. See documentation of {@link Executors#newCachedThreadPool()}<br>
	 * 
	 * if size has value >=1, the pool has a fixed size by the given value
	 * 
	 * @param size the size of the used worker thread pool
	 */
	public static void setWorkerThreadPoolSize(int size) {
		if (threadPool!=null) throw new IllegalStateException("You have to set the size BEFORE using createRegistry() or lookup()...");
		
		if (size==-1){
			threadPool = Executors.newCachedThreadPool();
		} else if (size==1) {
			threadPool = Executors.newSingleThreadExecutor();			
		} else {
			threadPool = Executors.newFixedThreadPool(size);
		}
	}
	
	

}
