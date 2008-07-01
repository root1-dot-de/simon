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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.root1.simon.exceptions.SimonRemoteException;
import de.root1.simon.utils.SimonClassLoader;
import de.root1.simon.utils.Utils;


public class SimonProxy implements InvocationHandler {
	
	protected transient Logger _log = Logger.getLogger(this.getClass().getName());
	
	/** name of the corresponding remoteobject in the remote-lookuptable */
	private String remoteObjectName;
	
	/** TODO member has to be described */
	private Dispatcher dispatcher;
	
	/** TODO member has to be described */
	private SelectionKey key;
	
	/**
	 * 
	 * Constructor which sets the reference to the dispatcher and the remoteobject name
	 * 
	 * @param dispatcher a reference to the underlying dispatcher
	 * @param key a reference to the key of the correspoding network conneciton
	 * @param remoteObjectName name of the remoteobject
	 */
	public SimonProxy(Dispatcher dispatcher, SelectionKey key, String remoteObjectName) {
		this.dispatcher = dispatcher;
		this.key = key;

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
					return remoteEquals(args[0]);
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
				throw new SimonRemoteException("Could not process invocation of method '"+method.getName()+"'. Underlying exception: "+e.getMessage());
			}
		}
		
		/*
		 * server then does the following:
		 * server gets according to the methodname and parametertypes the method
		 * and invokes the method. the result is communicated back to the client 
		 */
		//Utils.debug("SimonProxy.invoke() -> start. computing method hash: method="+method+" hash="+Utils.computeMethodHash(method));
		Object result = dispatcher.invokeMethod(key, remoteObjectName, Utils.computeMethodHash(method), method.getParameterTypes(),args, method.getReturnType());
//		Object result = dispatcherReference.get().invokeMethod(keyReference.get(), remoteObjectName, Utils.computeMethodHash(method), method.getParameterTypes(),args, method.getReturnType());
		
		
		// Check for exceptions ...
		if (result instanceof Throwable){
			throw (Throwable)result;
		}
		
		if (result instanceof SimonCallback){
			
			// creating a proxy for the callback
			SimonCallback simonCallback = (SimonCallback) result;
			Class<?>[] listenerInterfaces = new Class<?>[1];
			listenerInterfaces[0] = Class.forName(simonCallback.getInterfaceName());

			SimonProxy handler = new SimonProxy(dispatcher, key, simonCallback.getId());

			// reimplant the proxy object
			result = Proxy.newProxyInstance(SimonClassLoader.getClassLoader(this.getClass()), listenerInterfaces, handler);
			
			
		}
		_log.fine("end");
		return  result;
	}
	
	
	/**
	 * 
	 * TODO: Documentation to be done for method 'getInetAddress', by 'ACHR'..
	 * 
	 * @return
	 */
	protected InetAddress getInetAddress() {
		return ((SocketChannel)key.channel()).socket().getInetAddress();
	}
	
	/**
	 * 
	 * TODO: Documentation to be done for method 'getPort', by 'ACHR'..
	 * 
	 * @return
	 */
	protected int getRemotePort(){
		return ((SocketChannel)key.channel()).socket().getPort();
	}
	
	/**
	 * 
	 * TODO: Documentation to be done for method 'getLocalPort', by 'ACHR'..
	 * 
	 * @return
	 */
	protected int getLocalPort(){
		return ((SocketChannel)key.channel()).socket().getLocalPort();
	}
	
	/**
	 * 
	 * TODO: Documentation to be done for method 'remoteToString', by 'ACHR'..
	 * 
	 * @return
	 * @throws SimonRemoteException
	 */
	private String remoteToString() throws SimonRemoteException {
		try {
			return "[Proxy="+remoteObjectName+
						"|invocationHandler="+super.toString()+
						"|remote="+dispatcher.invokeToString(key, remoteObjectName)+
					"]";
		} catch (IOException e) {
			throw new SimonRemoteException(e.getMessage());
		}
	}
	
	/**
	 * 
	 * TODO: Documentation to be done for method 'remoteHashCode', by 'ACHR'..
	 * 
	 * @return
	 * @throws IOException
	 */
	private int remoteHashCode() throws IOException {
		return dispatcher.invokeHashCode(key, remoteObjectName);
	}
	
	/**
	 * 
	 * TODO: Documentation to be done for method 'remoteEquals', by 'ACHR'..
	 * 
	 * @param object
	 * @return
	 * @throws IOException
	 */
	private boolean remoteEquals(Object object) throws IOException {
		return dispatcher.invokeEquals(key, remoteObjectName, object);
	}

	/**
	 * 
	 * TODO: Documentation to be done for method 'release', by 'ACHR'..
	 *
	 */
	public Dispatcher release() {
		_log.fine("begin");
		dispatcher.cancelKey(key);
		remoteObjectName=null;
		_log.fine("end");
		return dispatcher;
	}
	
	@Override
	public String toString() {
	
		return "[Proxy="+remoteObjectName+
		"|invocationHandler="+super.toString()+
		"|dispatcher="+dispatcher.toString()+
		"]";
	}
	
}
