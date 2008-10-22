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
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.mina.core.session.IoSession;

import de.root1.simon.exceptions.SimonRemoteException;
import de.root1.simon.utils.SimonClassLoader;
import de.root1.simon.utils.Utils;

/**
 * The InvocationHandler which redirects each method call over the network to the related dispatcher
 * 
 * @author achristian
 *
 */
public class SimonProxy implements InvocationHandler {
	
	protected transient Logger _log = Logger.getLogger(this.getClass().getName());
	
	/** name of the corresponding remote object in the remote lookup table */
	private String remoteObjectName;

	

	/** a reference to the associated dispatcher */
	private Dispatcher dispatcher;
	
	/** a reference to the session which is the reference to the related network connection */
	private IoSession session;
	
	/**
	 * 
	 * Constructor which sets the reference to the dispatcher and the remote object name
	 * 
	 * @param dispatcher a reference to the underlying dispatcher
	 * @param key a reference to the key of the corresponding network connection
	 * @param remoteObjectName name of the remote object
	 */
	public SimonProxy(Dispatcher dispatcher, IoSession session, String remoteObjectName) {
		this.dispatcher = dispatcher;
		this.session = session;

		this.remoteObjectName = remoteObjectName;
	}

	/**
	 * 
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
	 */
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (_log.isLoggable(Level.FINE)) {
			_log.fine("begin");
			_log.fine("method="+method.getName()+" args="+args);
		}
		
		/*
		 * Check if the method is NOT a SIMON remote method
		 */
		if (!method.toString().contains("throws "+SimonRemoteException.class.getName())){
			
			try{
				// redirect invocation
				if (method.toString().equalsIgnoreCase(Statics.EQUALS_METHOD_SIGNATURE)){
					
					// check if object is an remote object which has to be looked up at the opposite ReadEventHandler
					Object o;
					if (args[0] instanceof SimonRemote) {
						
						o = new SimonRemoteInstance(session,(SimonRemote)args[0]);
//						o = "simonRemoteObjectName="+Simon.getSimonProxy(args[0]).getRemoteObjectName();
						
					} else { // else, it's a standard object
						
						o = args[0];
						// .. and if the standard object is not serializable, throw an exception
						if (!(o instanceof Serializable)) {
							throw new IllegalArgumentException("SIMON remote objects can only compared with objects that are serializable!");
						}
						
					}
					
					return remoteEquals(o);
				}
				else
				if (method.toString().equalsIgnoreCase(Statics.HASHCODE_METHOD_SIGNATURE)){
					return remoteHashCode();
				}
				else
				if (method.toString().equalsIgnoreCase(Statics.TOSTRING_METHOD_SIGNATURE)){
					return remoteToString();
				}else
					throw new SimonRemoteException("'"+method.getName()+"' is whether a remote method, nor is it callable over remote.");
				
			} catch (IOException e){
				throw new SimonRemoteException("Could not process invocation of method '"+method.getName()+"'. Underlying exception: "+e);
			}
		}
		
		/*
		 * server then does the following:
		 * server gets according to the method name and parameter types the method
		 * and invokes the method. the result is communicated back to the client 
		 */
		Object result = dispatcher.invokeMethod(session, remoteObjectName, method, args);
		
		
		// Check for exceptions ...
		if (result instanceof Throwable){
			throw (Throwable)result;
		}
		
		if (result instanceof SimonRemoteInstance){
			
			// creating a proxy for the callback
			SimonRemoteInstance simonCallback = (SimonRemoteInstance) result;
			Class<?>[] listenerInterfaces = new Class<?>[1];
			listenerInterfaces[0] = Class.forName(simonCallback.getInterfaceName());

			SimonProxy handler = new SimonProxy(dispatcher, session, simonCallback.getId());

			// reimplant the proxy object
			result = Proxy.newProxyInstance(SimonClassLoader.getClassLoader(this.getClass()), listenerInterfaces, handler);
			
			
		}
		_log.fine("end");
		return  result;
	}
	
	
	/**
	 * 
	 * Returns the {@link InetAddress} of the remote host connected with this proxy 
	 * 
	 * @return the {@link InetAddress} of the remote host
	 */
	protected SocketAddress getInetAddress() {
		return session.getRemoteAddress();
	}
	
	/**
	 * 
	 * Redirects the toString() call to the remote host to be called there. 
	 * The result is a String in the format:<br>
	 * <pre>
	 * [Proxy={ame of the remote object}|invocationHandler={result of proxy's super.toString()}|remote={result of remote toString() call}]
	 * </pre>
	 * 
	 * @return the result of the remote "toString()" call
	 * @throws SimonRemoteException
	 */
	private String remoteToString() {
		return "[Proxy="+remoteObjectName+
					"|invocationHandler="+super.toString()+
					"|remote="+dispatcher.invokeToString(session, remoteObjectName)+
				"]";
	}
	
	/**
	 * 
	 * Redirects hashCode() method call to the remote host and returns his result
	 * 
	 * @return the result of the remote hashCode() call
	 * @throws IOException
	 */
	private int remoteHashCode() throws IOException {
		return dispatcher.invokeHashCode(session, remoteObjectName);
	}
	
	/**
	 * 
	 * Redirects hashEquals() method call to the remote host and returns his result
	 * 
	 * @param object the object to compare with
	 * @return the result of the remote equals() call
	 * @throws IOException
	 */
	private boolean remoteEquals(Object object) throws IOException {
		return dispatcher.invokeEquals(session, remoteObjectName, object);
	}

	/**
	 * 
	 * Releases this proxy. This cancels the key on the dispatcher. 
	 *
	 */
	public Dispatcher release() {
		_log.fine("begin");
		remoteObjectName=null;
		_log.fine("end");
		return dispatcher;
	}
	
	@Override
	public String toString() {
		return remoteToString();
	}
	
	/**
	 * Returns the proxy's remote object name in the related lookup table
	 * @return the remote object name
	 */
	protected String getRemoteObjectName() {
		return remoteObjectName;
	}

}
