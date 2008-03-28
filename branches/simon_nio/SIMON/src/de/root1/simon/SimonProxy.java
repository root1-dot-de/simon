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
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;

import de.root1.simon.utils.SimonClassLoader;
import de.root1.simon.utils.Utils;


public class SimonProxy implements InvocationHandler {
	
	
	/** name of the corresponding remoteobject in the remote-lookuptable */
	private String remoteObjectName;
	
	/** local socket-endpoint for communication with remote */
	private Endpoint endpoint;
	
	/**
	 * 
	 * Constructor which sets the reference to the endpoint and the remoteobject name
	 * 
	 * @param endpoint reference to the endpoint
	 * @param remoteObjectName name of the remoteobject
	 */
	public SimonProxy(Endpoint endpoint, String remoteObjectName) {
		this.endpoint = endpoint;
		this.remoteObjectName = remoteObjectName;
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				
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
		if (Statics.DEBUG_MODE)
        	System.out.println("SimonProxy.invoke() -> start. computing method hash: method="+method+" hash="+Utils.computeMethodHash(method));
		Object result = endpoint.sendInvocationToRemote(remoteObjectName, Utils.computeMethodHash(method), method.getParameterTypes(),args, method.getReturnType());
		
		
		// Check for exceptions ...
		if (result instanceof Throwable){
			throw (Throwable)result;
		}
		
		if (result instanceof SimonCallback){
			
			// creating a proxy for the callback
			SimonCallback simonCallback = (SimonCallback) result;
			Class<?>[] listenerInterfaces = new Class<?>[1];
			listenerInterfaces[0] = Class.forName(simonCallback.getInterfaceName());

			SimonProxy handler = new SimonProxy(endpoint, simonCallback.getId());

			// reimplant the proxy object
			result = Proxy.newProxyInstance(SimonClassLoader.getClassLoader(this.getClass()), listenerInterfaces, handler);
			
			
		}
		if (Statics.DEBUG_MODE)
        	System.out.println("SimonProxy.invoke() -> end. result="+result);
		return  result;
	}
	
	
	// FIXME reimplement aksing for ip-address of client
//	protected InetAddress getInetAddress() {
//		return endpoint.getInetAddress();
//	}
//	
//	protected int getPort(){
//		return endpoint.getPort();
//	}
	
	private String remoteToString() throws SimonRemoteException {
		// TODO Auto-generated method stub
		try {
			return "[Proxy="+remoteObjectName+
//						"|endpoint="+getInetAddress()+":"+getPort()+
						"|endpoint"+
						"|invocationHandler="+super.toString()+
						"|remote="+endpoint.invokeToString(remoteObjectName)+
					"]";
		} catch (IOException e) {
			throw new SimonRemoteException(e.getMessage());
		}
	}
	
	private int remoteHashCode() throws IOException {
		return endpoint.invokeHashCode(remoteObjectName);
	}
	
	private boolean remoteEquals(Object object) throws IOException {
		return endpoint.sendEqualsRequest(remoteObjectName, object);
	}

}
