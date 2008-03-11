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
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import de.root1.simon.utils.SimonClassLoader;


/**
 * Executes a requested invocation and puts the result back to the endpoint
 *
 * @author achristian
 *
 */
public final class ProcessMethodInvocationRunnable implements Runnable {
	
	final private Endpoint endpoint;
	final private int requestId;
	final private String serverObject; 
	final private Method method; 
	final private Object[] args; 

	/**
	 * 
	 * Instantiates a new invocation process with all the needed parameters
	 * 
	 * @param endpoint the associated endpoint
	 * @param requestId the id of the request (used to memory the return-value) 
	 * @param serverObject the associated server object
	 * @param method the method to call
	 * @param args the arguments for the method
	 */
	public ProcessMethodInvocationRunnable(	final Endpoint endpoint, 
											final int requestId, 
											final String serverObject, 
											final Method method, 
											final Object[] args) {
		
		
		this.endpoint = endpoint;
		this.requestId = requestId;
		this.serverObject = serverObject;
		this.method = method;
		this.args = args;

	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		processMethodInvocation();
	}
	
	/**
	 * Executes the requested invocation
	 */
	private void processMethodInvocation() {
		if (Statics.DEBUG_MODE) System.out.println("ProcessMethodInvocationRunnable.processMethodInvocation() -> begin. requestID="+requestId);
		Object result = null;
		
		try {			
			// replace existing SimonRemote objects with proxy object
			if (args != null) {
	
				for (int i = 0; i < args.length; i++) {
					
					// search the arguments for remote-objects for callbacks
					if (args[i] instanceof SimonCallback) {
					
						final SimonCallback simonCallback = (SimonCallback) args[i];
											
						Class<?>[] listenerInterfaces = new Class<?>[1];
						listenerInterfaces[0] = Class.forName(simonCallback.getInterfaceName());

						// reimplant the proxy object
						args[i] = Proxy.newProxyInstance(SimonClassLoader.getClassLoader(this.getClass()), listenerInterfaces, new SimonProxy(endpoint, simonCallback.getId()));
						
					} 
				} 
			} 
			
			try {
				if (Statics.DEBUG_MODE) System.out.println("ProcessMethodInvocationRunnable.processMethodInvocation() -> start invoking method='"+method+"'. requestID="+requestId);
				result = method.invoke(endpoint.getLookupTable().getRemoteBinding(serverObject), args);
				if (Statics.DEBUG_MODE) System.out.println("ProcessMethodInvocationRunnable.processMethodInvocation() -> end invoking method='"+method+"'. requestID="+requestId+" result="+result);				
				// Search for SimonRemote in result
				if (result instanceof SimonRemote){
					endpoint.getLookupTable().putRemoteBinding(result.toString(), (SimonRemote)result);
					result = new SimonCallback((SimonRemote)result);
				}
				
			} catch (InvocationTargetException e){
				result = e.getTargetException();
			} 
			
			final ObjectOutputStream oos = endpoint.getObjectOutputStream();
			synchronized (oos) {
				oos.write(Statics.INVOCATION_RETURN_PACKET);
				oos.writeInt(requestId);
				endpoint.wrapValue(method.getReturnType(), result, oos);
				oos.flush();
			}
		} catch (IOException e){
			/* 
			 * TODO Was tun bei einer IOException?
			 * Was macht der Client wenn er keine Antwort bekommt? 
			 */
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (Statics.DEBUG_MODE) System.out.println("ProcessMethodInvocationRunnable.processMethodInvocation() -> end. requestID="+requestId);
	}

}
