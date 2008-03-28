package de.root1.simon;

import java.io.IOException;

public class InvocationHandler {
	
	/**
	 * sends a requested invocation to the server
	 * 
	 * @param remoteObject
	 * @param methodName
	 * @param parameterTypes
	 * @param args
	 * @param returnType 
	 * @return the method's result
	 * @throws SimonRemoteException if there's a problem with the communication
	 * @throws IOException 
	 */
	protected Object invokeMethod(String remoteObject, long methodHash, Class<?>[] parameterTypes, Object[] args, Class<?> returnType) throws SimonRemoteException, IOException {
		final int requestID = generateRequestID();
		if (Statics.DEBUG_MODE) System.out.println("Endpoint.sendInvocationToRemote() -> begin. requestID="+requestID);
		if (globalEndpointException!=null) throw globalEndpointException;

		final Object monitor = new Object();
		
		// memory the return-type for later unwrap
		requestReturnType.put(requestID, returnType);
		idMonitorMap.put(requestID, monitor);
		
		/*
		 * register callback objects in the lookup-table
		 */
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				if (args[i] instanceof SimonRemote) {
					SimonCallback sc = new SimonCallback((SimonRemote)args[i]);
					lookupTable.putRemoteBinding(sc.getId(), (SimonRemote)args[i]);
					args[i] = sc; // overwrite arg with wrapped callback-interface
				}
			}
		}

		synchronized (monitor) {

			synchronized (objectOutputStream) {			
				sendCounter++;
				if (sendCounter==Integer.MAX_VALUE) sendCounter=0;
				
				objectOutputStream.write(Statics.INVOCATION_PACKET); // msg type
				objectOutputStream.writeInt(requestID); // requestid
				
				objectOutputStream.writeUTF(remoteObject);
				objectOutputStream.writeLong(methodHash);
				
				for (int i = 0; i < parameterTypes.length; i++) {
		            wrapValue(parameterTypes[i], args[i], objectOutputStream);
		        }
				
				objectOutputStream.flush();
				if (sendCounter%objectCacheLifetime==0){
					objectOutputStream.reset();
				}
				
			}
				// got to sleep until result is present
				try {
					monitor.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
		if (Statics.DEBUG_MODE) System.out.println("Endpoint.sendInvocationToRemote() -> end. requestID="+requestID);
		return requestResults.remove(requestID);
	}

	/**
	 * 
	 * TODO: Documentation to be done for method 'sendLookup', by 'ACHR'..
	 * 
	 * @param name
	 * @return the object we made the lookup for
	 * @throws SimonRemoteException 
	 * @throws IOException 
	 */
	public Object invokeLookup(String name) throws SimonRemoteException, IOException {
		if (globalEndpointException!=null) throw globalEndpointException;

		final int requestID = generateRequestID();
		final Object monitor = new Object();
		
		synchronized (idMonitorMap) {
			idMonitorMap.put(requestID, monitor);
		}

		synchronized (monitor) {
		
			synchronized (objectOutputStream) {
				objectOutputStream.write(Statics.LOOKUP_PACKET); // msg type
				objectOutputStream.writeInt(requestID); // requestid
				objectOutputStream.writeUTF(name); // name of remote object in lookuptable	
				objectOutputStream.flush();
			}			

			// got to sleep until result is present
			try {
				monitor.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			
		}
		// check if there was an error while sleeping
		if (globalEndpointException!=null) throw globalEndpointException;
		
		// get result
		return requestResults.remove(requestID);
	}
	
	/**
	 * 
	 * TODO: Documentation to be done for method 'sendLookup', by 'ACHR'..
	 * 
	 * @param remoteObjectName
	 * @return
	 * @throws IOException 
	 */
	protected String invokeToString(String remoteObjectName) throws IOException {
		if (globalEndpointException!=null) throw globalEndpointException;

		final int requestID = generateRequestID();
		final Object monitor = new Object();
		
		synchronized (idMonitorMap) {
			idMonitorMap.put(requestID, monitor);
		}

		synchronized (monitor) {
		
			synchronized (objectOutputStream) {
				objectOutputStream.write(Statics.TOSTRING_PACKET); // msg type
				objectOutputStream.writeInt(requestID); // requestid
				objectOutputStream.writeUTF(remoteObjectName); // name of remote object in lookuptable	
				objectOutputStream.flush();
			}			

			// got to sleep until result is present
			try {
				monitor.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			
		}
		// check if there was an error while sleeping
		if (globalEndpointException!=null) throw globalEndpointException;
		
		// get result
		return (String) requestResults.remove(requestID);
	}

	/**
	 * 
	 * TODO: Documentation to be done for method 'sendLookup', by 'ACHR'..
	 * 
	 * @param remoteObjectName
	 * @return
	 * @throws IOException 
	 */
	protected int invokeHashCode(String remoteObjectName) throws IOException {
		if (globalEndpointException!=null) throw globalEndpointException;

		final int requestID = generateRequestID();
		final Object monitor = new Object();
		
		synchronized (idMonitorMap) {
			idMonitorMap.put(requestID, monitor);
		}

		synchronized (monitor) {
		
			synchronized (objectOutputStream) {
				objectOutputStream.write(Statics.HASHCODE_PACKET); // msg type
				objectOutputStream.writeInt(requestID); // requestid
				objectOutputStream.writeUTF(remoteObjectName); // name of remote object in lookuptable	
				objectOutputStream.flush();
			}			

			// got to sleep until result is present
			try {
				monitor.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			
		}
		// check if there was an error while sleeping
		if (globalEndpointException!=null) throw globalEndpointException;
		
		// get result
		return (Integer) requestResults.remove(requestID);
	}

	protected boolean invokeEquals(String remoteObjectName, Object object) throws IOException {
		if (globalEndpointException!=null) throw globalEndpointException;

		final int requestID = generateRequestID();
		final Object monitor = new Object();
		
		synchronized (idMonitorMap) {
			idMonitorMap.put(requestID, monitor);
		}

		synchronized (monitor) {
		
			synchronized (objectOutputStream) {
				objectOutputStream.write(Statics.EQUALS_PACKET); // msg type
				objectOutputStream.writeInt(requestID); // requestid
				objectOutputStream.writeUTF(remoteObjectName); // name of remote object in lookuptable
				objectOutputStream.writeObject(object); // object to compare with
				objectOutputStream.flush();
			}			

			// got to sleep until result is present
			try {
				monitor.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			
		}
		// check if there was an error while sleeping
		if (globalEndpointException!=null) throw globalEndpointException;
		
		// get result
		return (Boolean) requestResults.remove(requestID);
	}

}
