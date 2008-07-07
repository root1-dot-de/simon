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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;

/**
 * An endpoint represents one end of the socket-connection between client and server.
 * 
 * @author ACHR
 */
public class Endpoint extends Thread {

	/** the <code>ObjectInputStream</code> associated with the opened socket connection */
	private ObjectInputStream objectInputStream;
	
	/** the <code>ObjectOutputStream</code> associated with the opened socket connection */
	private ObjectOutputStream objectOutputStream;
	
	/** The table that holds all the registered/bind remote objects */
	private LookupTable lookupTable;
	
	/** a simple counter that is used for creating request IDs */
	private int requestIdCounter = 0;
	
	/** The map that holds the relation between the request ID and the corresponding monitor */
	private HashMap<Integer, Object> idMonitorMap = new HashMap<Integer, Object>();
	
	/** The map that holds the relation between the request ID and the received result */
	private HashMap<Integer, Object> requestResults = new HashMap<Integer, Object>();
	
	/** a memory map for the client the unwrap the incoming return value after executing a method on the server */
	private HashMap<Integer, Class<?>> requestReturnType = new HashMap<Integer, Class<?>>();
	
	/** is used for exception between all the threads in here */
	private SimonRemoteException globalEndpointException = null;

	/** the associated socket */
	private Socket socket;
	

	private int sendCounter = 0;
	private int objectCacheLifetime;
	


	/**
	 * 
	 * Creates a new so called <i>Endpoint</i>.  
	 * @param objectCacheLifetime 
	 * 
	 * @param lookupTable the reference to an instance of <code>LookupTable</code>
	 * @param threadName used for naming the <code>Endpoint</code>-thread.
	 * @throws IOException 
	 */
	public Endpoint(Socket socket, int objectCacheLifetime, LookupTable lookupTable, String threadName) throws IOException {
		if (Statics.DEBUG_MODE) System.out.println("Endpoint.Endpoint() -> start");
		if (objectCacheLifetime<1) throw new IllegalArgumentException("objectCacheLifetime must be >=1");
		
		this.setName("Endpoint: "+threadName);
		this.lookupTable = lookupTable;

		if (Statics.DEBUG_MODE) System.out.println("Endpoint.Endpoint() trying to get oos");
		this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
		if (Statics.DEBUG_MODE) System.out.println("Endpoint.Endpoint() got oos");
		
		if (Statics.DEBUG_MODE) System.out.println("Endpoint.Endpoint() trying to get ois");
		this.objectInputStream = new ObjectInputStream(socket.getInputStream());
		if (Statics.DEBUG_MODE) System.out.println("Endpoint.Endpoint() got ois");
		
		this.socket = socket;
		this.objectCacheLifetime = objectCacheLifetime;
		
//		new Thread(){
//			@Override
//			public void run() {
//				while(true) {
//					
//					System.out.println("\n\n\trequestResults="+requestResults.size());
//					System.out.println("\trequestReturnType="+requestReturnType.size()+"");
//					System.out.println("\tidMonitorMap="+idMonitorMap.size()+"\n\n");
//					
//					try {
//						Thread.sleep(750);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//			}
//		}.start();
		if (Statics.DEBUG_MODE) System.out.println("Endpoint.Endpoint() -> end");
	}

	/**
	 * 
	 * Generates a request ID
	 * 
	 * @return a request ID
	 */
	private Integer generateRequestID() {
		return requestIdCounter++;
	}

	/**
	 * The main receive-loop
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {

		int requestID = -1;
		int msgType = -1;
		String remoteObjectName = null;

		try {

			while (!interrupted()) {
//					synchronized (objectOutputStream) {
//						synchronized (objectInputStream) {
//							objectInputStream.reset();	
//						}
//					}
					// Header: Get type and requestid
					msgType = objectInputStream.read();	
					requestID = objectInputStream.readInt();
					if (Statics.DEBUG_MODE) System.out.println("Endpoint.run() -> paket header: msgType="+msgType+" requestID="+requestID);
		
					if (globalEndpointException==null)
					
					// if the received data is a new request ...
					switch (msgType) {
						
						case Statics.INVOCATION_PACKET:
							if (Statics.DEBUG_MODE) System.out.println("Endpoint.run() -> INVOCATION_PACKET -> start. requestID="+requestID);
							remoteObjectName = objectInputStream.readUTF();
							final long methodHash = objectInputStream.readLong();
							
							final Method method = lookupTable.getMethod(remoteObjectName, methodHash);
							final Class<?>[] parameterTypes = method.getParameterTypes();			
							final Object[] args = new Object[parameterTypes.length];
							
							// unwrapping the arguments
							for (int i = 0; i < args.length; i++) {
								args[i]=unwrapValue(parameterTypes[i], objectInputStream);							
							}
							
							if (Statics.DEBUG_MODE) System.out.println("Endpoint.run() -> INVOCATION_PACKET -> remoteObject="+remoteObjectName+" methodHash="+methodHash+" method='"+method+"' args.length="+args.length);
							
							// put the data into a runnable					
							Simon.getThreadPool().execute(new ProcessMethodInvocationRunnable(this,requestID, remoteObjectName, method, args));
							if (Statics.DEBUG_MODE) System.out.println("Endpoint.run() -> INVOCATION_PACKET -> end. requestID="+requestID);
							break;
							
						case Statics.LOOKUP_PACKET :
							processLookup(requestID, objectInputStream.readUTF());
							break;
							
						case Statics.TOSTRING_PACKET :
							processToString(requestID, objectInputStream.readUTF());
							break;
						
						case Statics.HASHCODE_PACKET :
							processHashCode(requestID, objectInputStream.readUTF());
							break;
						
						case Statics.EQUALS_PACKET :
							remoteObjectName = objectInputStream.readUTF();
							final Object object = objectInputStream.readObject();
							processEquals(requestID, remoteObjectName, object);
							break;	
							
						case Statics.INVOCATION_RETURN_PACKET :
							if (Statics.DEBUG_MODE) System.out.println("Endpoint.run() -> INVOCATION_RETURN_PACKET -> start. requestID="+requestID);
							synchronized (requestResults) {
								synchronized (requestReturnType) {					
									//unwrap the return-value
									requestResults.put(requestID, unwrapValue(requestReturnType.remove(requestID), objectInputStream));
									if (Statics.DEBUG_MODE) System.out.println("Endpoint.run() -> INVOCATION_RETURN_PACKET -> requestID="+requestID+" result="+requestResults.get(requestID));
								}
							}
							wakeWaitingProcess(requestID);
							if (Statics.DEBUG_MODE) System.out.println("Endpoint.run() -> INVOCATION_RETURN_PACKET -> end. requestID="+requestID);
							break;
							
						case Statics.LOOKUP_RETURN_PACKET :
							synchronized (requestResults) {
								requestResults.put(requestID, objectInputStream.readObject());
							}
							wakeWaitingProcess(requestID);
							break;
							
						case Statics.TOSTRING_RETURN_PACKET :
							synchronized (requestResults) {
								requestResults.put(requestID, objectInputStream.readUTF());
							}
							wakeWaitingProcess(requestID);
							break;
						
						case Statics.HASHCODE_RETURN_PACKET :
							synchronized (requestResults) {
								requestResults.put(requestID, objectInputStream.readInt());
							}
							wakeWaitingProcess(requestID);
							break;
							
						case Statics.EQUALS_RETURN_PACKET :
							synchronized (requestResults) {
								requestResults.put(requestID, objectInputStream.readBoolean());
							}
							wakeWaitingProcess(requestID);
							break;
							
						default :
							interrupt();
							globalEndpointException = new SimonRemoteException("invalid packet received from endpoint ...");
							try {
								objectInputStream.close();
								objectOutputStream.close();
							} catch (IOException e) {
							}
							break;
					}
		
		
		
			} // end while

		} catch (IOException e){
			globalEndpointException = new SimonRemoteException("IOException while running receive-loop on endpoint '"+this.getName()+"': "+e.getMessage());
			wakeAllMonitors();
		} catch (ClassNotFoundException e) {
			globalEndpointException = new SimonRemoteException("ClassNotFoundException while running receive-loop on endpoint '"+this.getName()+"': "+e.getMessage());
			wakeAllMonitors();
		}
	}

	private void processEquals(int requestID, String remoteObjectName, Object object) throws IOException{
			synchronized (objectOutputStream) {
				objectOutputStream.write(Statics.EQUALS_RETURN_PACKET);
				objectOutputStream.writeInt(requestID);
				
				final boolean equals = lookupTable.getRemoteBinding(remoteObjectName).equals(object);		
				
				objectOutputStream.writeBoolean(equals);
				objectOutputStream.flush();
			}	
	}

	/**
	 * 
	 * processes a request for a "hashCode()" call on a remote-object
	 * 
	 * @param requestID
	 * @param remoteObjectName
	 * @throws IOException
	 */
	private void processHashCode(int requestID, String remoteObjectName) throws IOException {
		synchronized (objectOutputStream) {
			objectOutputStream.write(Statics.HASHCODE_RETURN_PACKET);
			objectOutputStream.writeInt(requestID);
			
			final int hashcode = lookupTable.getRemoteBinding(remoteObjectName).hashCode();		
			
			objectOutputStream.writeInt(hashcode);
			objectOutputStream.flush();
		}	
	}

	/**
	 * 
	 *processes a request for a "toString()" call on a remote-object
	 * 
	 * @param requestID
	 * @param remoteObjectName
	 * @throws IOException
	 */
	private void processToString(int requestID, String remoteObjectName) throws IOException {
		synchronized (objectOutputStream) {
			objectOutputStream.write(Statics.TOSTRING_RETURN_PACKET);
			objectOutputStream.writeInt(requestID);
			
			final String tostring = lookupTable.getRemoteBinding(remoteObjectName).toString();		
			
			objectOutputStream.writeUTF(tostring);
			objectOutputStream.flush();
		}	
	}

	/**
	 * Wake the process with the related requestID
	 * 
	 * @param requestID the process to wake  
	 */
	private void wakeWaitingProcess(int requestID) {
		synchronized (idMonitorMap) {						
			final Object monitor = idMonitorMap.get(requestID);
			synchronized (monitor) {
				monitor.notify(); // wake the waiting method
			}
			idMonitorMap.remove(requestID);
		}
	}

	/** 
	 * wake all waiting processes. This is only called due to global errors ...
	 */
	private void wakeAllMonitors() {
		synchronized (idMonitorMap) {
			for (Integer id : idMonitorMap.keySet()) {
				wakeWaitingProcess(id.intValue());
			}
		}
	}

	/**
	 * 
	 * processes a lookup
	 * @throws IOException 
	 */
	private void processLookup(int requestID, String remoteObjectName) throws IOException{
		
		synchronized (objectOutputStream) {
			objectOutputStream.write(Statics.LOOKUP_RETURN_PACKET);
			objectOutputStream.writeInt(requestID);
			objectOutputStream.writeObject(lookupTable.getRemoteBinding(remoteObjectName).getClass().getInterfaces());
			objectOutputStream.flush();
		}	

	}

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
	protected Object sendInvocationToRemote(String remoteObject, long methodHash, Class<?>[] parameterTypes, Object[] args, Class<?> returnType) throws SimonRemoteException, IOException {
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
	public Object sendLookup(String name) throws SimonRemoteException, IOException {
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
	protected String sendToStringRequest(String remoteObjectName) throws IOException {
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
	protected int sendHashCodeRequest(String remoteObjectName) throws IOException {
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

	protected boolean sendEqualsRequest(String remoteObjectName, Object object) throws IOException {
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
	
	/**
	 * for internal use only
	 */
	protected LookupTable getLookupTable() {
		return lookupTable;
	}

	/**
	 * for internal use only
	 * @return
	 */
	protected ObjectOutputStream getObjectOutputStream() {
		return objectOutputStream;
	}

	/**
	 * for internal use only
	 * @return
	 */
	protected InetAddress getInetAddress() {
		return socket.getInetAddress();
	}
	
	/**
	 * for internal use only
	 * @return
	 */
	protected int getPort(){
		return socket.getPort();
	}
	
	
	/**
     * wrap the value with the according write method
     */
    protected void wrapValue(Class<?> type, Object value, ObjectOutputStream objectOutputStream) throws IOException {
    	if (Statics.DEBUG_MODE) System.out.println("Endpoint.wrapValue() -> start");
    	if (Statics.DEBUG_MODE) System.out.println("Endpoint.wrapValue() -> value="+value);
    	
    	if (type == void.class || value instanceof Throwable) {
    		objectOutputStream.writeObject(value);
        	if (Statics.DEBUG_MODE) System.out.println("Endpoint.wrapValue() -> void, writing as object, may be 'null' or a 'Throwable'");
    	}
    	else
    	if (type.isPrimitive()) {
        	if (type == boolean.class) {
        		if (Statics.DEBUG_MODE) System.out.println("Endpoint.wrapValue() -> boolean");
            	objectOutputStream.writeBoolean(((Boolean) value).booleanValue());
            } else if (type == byte.class) {
            	if (Statics.DEBUG_MODE) System.out.println("Endpoint.wrapValue() -> byte");
            	objectOutputStream.writeByte(((Byte) value).byteValue());
            } else if (type == char.class) {
            	if (Statics.DEBUG_MODE) System.out.println("Endpoint.wrapValue() -> char");
            	objectOutputStream.writeChar(((Character) value).charValue());
            } else if (type == short.class) {
            	if (Statics.DEBUG_MODE) System.out.println("Endpoint.wrapValue() -> short");
            	objectOutputStream.writeShort(((Short) value).shortValue());
            } else if (type == int.class) {
            	if (Statics.DEBUG_MODE) System.out.println("Endpoint.wrapValue() -> int");
                objectOutputStream.writeInt(((Integer) value).intValue());
            } else if (type == long.class) {
            	if (Statics.DEBUG_MODE) System.out.println("Endpoint.wrapValue() -> long");
            	objectOutputStream.writeLong(((Long) value).longValue());
            } else if (type == float.class) {
            	if (Statics.DEBUG_MODE) System.out.println("Endpoint.wrapValue() -> float");
            	objectOutputStream.writeFloat(((Float) value).floatValue());
            } else if (type == double.class) {
            	if (Statics.DEBUG_MODE) System.out.println("Endpoint.wrapValue() -> double");
            	objectOutputStream.writeDouble(((Double) value).doubleValue());
            } else {
            	if (Statics.DEBUG_MODE) System.out.println("Endpoint.wrapValue() -> unknown");
                throw new IOException("Unknown primitive: " + type);
            }
        } else {
        	if (Statics.DEBUG_MODE) System.out.println("Endpoint.wrapValue() -> non primitive object");
        	objectOutputStream.writeObject(value);
        }
    	if (Statics.DEBUG_MODE) System.out.println("Endpoint.wrapValue() -> end");
    }

    /**
     * unwrap the value with the according read method
     */
    protected Object unwrapValue(Class<?> type, ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
    	if (Statics.DEBUG_MODE) System.out.println("Endpoint.unwrapValue() -> start");
    	
    	if (type == void.class ) {
    		if (Statics.DEBUG_MODE) System.out.println("Endpoint.unwrapValue() -> void, reading an object, may be 'null' or a 'Throwable'");
    		return objectInputStream.readObject();
    	}
    	else
    	if (type.isPrimitive()) {
        	if (type == boolean.class) {
        		if (Statics.DEBUG_MODE) System.out.println("Endpoint.unwrapValue() -> boolean -> end");
                return Boolean.valueOf(objectInputStream.readBoolean());
            } else if (type == byte.class) {
            	if (Statics.DEBUG_MODE) System.out.println("Endpoint.unwrapValue() -> byte -> end");
                return Byte.valueOf(objectInputStream.readByte());
            } else if (type == char.class) {
            	if (Statics.DEBUG_MODE) System.out.println("Endpoint.unwrapValue() -> char -> end");
                return Character.valueOf(objectInputStream.readChar());
            } else if (type == short.class) {
            	if (Statics.DEBUG_MODE) System.out.println("Endpoint.unwrapValue() -> short -> end");
                return Short.valueOf(objectInputStream.readShort());
            } else if (type == int.class) {
            	if (Statics.DEBUG_MODE) System.out.println("Endpoint.unwrapValue() -> int -> end");
                return Integer.valueOf(objectInputStream.readInt());
            } else if (type == long.class) {
            	if (Statics.DEBUG_MODE) System.out.println("Endpoint.unwrapValue() -> long -> end");
                return Long.valueOf(objectInputStream.readLong());
            } else if (type == float.class) {
            	if (Statics.DEBUG_MODE) System.out.println("Endpoint.unwrapValue() -> float -> end");
                return Float.valueOf(objectInputStream.readFloat());
            } else if (type == double.class) {
            	if (Statics.DEBUG_MODE) System.out.println("Endpoint.unwrapValue() -> double -> end");
                return Double.valueOf(objectInputStream.readDouble());
            } else {
            	if (Statics.DEBUG_MODE) System.out.println("Endpoint.unwrapValue() -> unknown -> end");
                throw new IOException("Unknown primitive: " + type);
            }
        } else {
        	if (Statics.DEBUG_MODE) System.out.println("Endpoint.unwrapValue() -> non primitive object -> end");
            return objectInputStream.readObject();
        }
    }


	

}
