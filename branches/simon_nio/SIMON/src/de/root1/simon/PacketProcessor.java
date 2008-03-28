package de.root1.simon;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.root1.simon.utils.Utils;

/**
 * 
 * A inner class that reads and processes the packets
 * 
 * @author ACHR
 */
class PacketProcessor implements Runnable {

	private SelectionKey key = null;
	private ByteBuffer bbHeader = ByteBuffer.allocate(5); // one byte + 4 byte integer containing the packet length
	private ByteBuffer bbPacket; // the packet itself
	private int requestID = -1;
	private int msgType = -1;
	private int packetLength = -1;
	private String remoteObjectName;
	private final LookupTable lookupTable;
	private final Endpoint endpoint;
	private SocketChannel socketChannel;

	public PacketProcessor(LookupTable lookupTable, SelectionKey key, Endpoint endpoint) {
		this.key = key;
		this.lookupTable = lookupTable;
		this.endpoint = endpoint;
	}

	public void run() {
		
		socketChannel = (SocketChannel) key.channel();
		try {
			
			// read the header which includes packet type id and packet size
			socketChannel.read(bbHeader);
			
			// Header: Get type and length
			msgType = bbHeader.get();	
			packetLength = bbHeader.getInt();
			
			bbPacket = ByteBuffer.allocate(packetLength);
			
			requestID = bbPacket.getInt();
			
			// if the received data is a new request ...
			switch (msgType) {
				
				case Statics.INVOCATION_PACKET:
					if (Statics.DEBUG_MODE) System.out.println("Endpoint.run() -> INVOCATION_PACKET -> start. requestID="+requestID);
					remoteObjectName = Utils.getString(bbPacket);
					final long methodHash = bbPacket.getLong();
					
					final Method method = lookupTable.getMethod(remoteObjectName, methodHash);
					final Class<?>[] parameterTypes = method.getParameterTypes();			
					final Object[] args = new Object[parameterTypes.length];
					
					// unwrapping the arguments
					for (int i = 0; i < args.length; i++) {
						args[i]=Utils.unwrapValue(parameterTypes[i], bbPacket);							
					}
					
					if (Statics.DEBUG_MODE) System.out.println("Endpoint.run() -> INVOCATION_PACKET -> remoteObject="+remoteObjectName+" methodHash="+methodHash+" method='"+method+"' args.length="+args.length);
					
					// put the data into a runnable
					// TODO is this thread-safe?
					endpoint.getInvocationPool().execute(new ProcessMethodInvocationRunnable(endpoint,requestID, remoteObjectName, method, args));
					
					if (Statics.DEBUG_MODE) System.out.println("Endpoint.run() -> INVOCATION_PACKET -> end. requestID="+requestID);
					break;
					
				case Statics.LOOKUP_PACKET :
					processLookup(requestID, Utils.getString(bbPacket));
					break;
					
				case Statics.TOSTRING_PACKET :
					processToString(requestID, Utils.getString(bbPacket));
					break;
				
				case Statics.HASHCODE_PACKET :
					processHashCode(requestID, Utils.getString(bbPacket));
					break;
				
				case Statics.EQUALS_PACKET :
					remoteObjectName = Utils.getString(bbPacket);
					final Object object = Utils.getObject(bbPacket);
					processEquals(requestID, remoteObjectName, object);
					break;	
					
//				case Statics.INVOCATION_RETURN_PACKET :
//					if (Statics.DEBUG_MODE) System.out.println("Endpoint.run() -> INVOCATION_RETURN_PACKET -> start. requestID="+requestID);
//					synchronized (requestResults) {
//						synchronized (requestReturnType) {					
//							//unwrap the return-value
//							requestResults.put(requestID, unwrapValue(requestReturnType.remove(requestID), objectInputStream));
//							if (Statics.DEBUG_MODE) System.out.println("Endpoint.run() -> INVOCATION_RETURN_PACKET -> requestID="+requestID+" result="+requestResults.get(requestID));
//						}
//					}
//					wakeWaitingProcess(requestID);
//					if (Statics.DEBUG_MODE) System.out.println("Endpoint.run() -> INVOCATION_RETURN_PACKET -> end. requestID="+requestID);
//					break;
//					
//				case Statics.LOOKUP_RETURN_PACKET :
//					synchronized (requestResults) {
//						requestResults.put(requestID, objectInputStream.readObject());
//					}
//					wakeWaitingProcess(requestID);
//					break;
//					
//				case Statics.TOSTRING_RETURN_PACKET :
//					synchronized (requestResults) {
//						requestResults.put(requestID, objectInputStream.readUTF());
//					}
//					wakeWaitingProcess(requestID);
//					break;
//				
//				case Statics.HASHCODE_RETURN_PACKET :
//					synchronized (requestResults) {
//						requestResults.put(requestID, objectInputStream.readInt());
//					}
//					wakeWaitingProcess(requestID);
//					break;
//					
//				case Statics.EQUALS_RETURN_PACKET :
//					synchronized (requestResults) {
//						requestResults.put(requestID, objectInputStream.readBoolean());
//					}
//					wakeWaitingProcess(requestID);
//					break;
					
				default :
//					interrupt();
//					globalEndpointException = new SimonRemoteException("invalid packet received from endpoint ...");
					try {
						socketChannel.close();
						key.cancel();
					} catch (IOException e) {
					}
					throw new SimonRemoteException("invalid packet received from endpoint ...");
//					break;
			}

			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		
		final int hashcode = lookupTable.getRemoteBinding(remoteObjectName).hashCode();		
		
		ByteBuffer bb = ByteBuffer.allocate(1+4+4);
		bb.put(Statics.HASHCODE_RETURN_PACKET);
		bb.putInt(requestID);
		bb.putInt(hashcode);
		
		endpoint.send(socketChannel,bb);
		
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

		final String tostring = lookupTable.getRemoteBinding(remoteObjectName).toString();		
		
		ByteBuffer bb = ByteBuffer.allocate(1+4+4+tostring.length());
		bb.put(Statics.TOSTRING_RETURN_PACKET);
		bb.putInt(requestID);
		bb.put(Utils.stringToBytes(tostring));
		
		endpoint.send(socketChannel,bb);
		
	}
	
	/**
	 * 
	 * processes a request for a "equls()" call on a remote-object
	 * 
	 * @param requestID
	 * @param remoteObjectName
	 * @praram object
	 * @throws IOException
	 */
	private void processEquals(int requestID, String remoteObjectName, Object object) throws IOException{

		final boolean equals = lookupTable.getRemoteBinding(remoteObjectName).equals(object);		

		ByteBuffer bb = ByteBuffer.allocate(1+4+1);
		bb.put(Statics.EQUALS_RETURN_PACKET);
		bb.putInt(requestID);
		bb.put(equals ? (byte) 1 : (byte) 0);
		
		endpoint.send(socketChannel,bb);
		
	}
	
	/**
	 * 
	 * processes a lookup
	 * @throws IOException 
	 */
	private void processLookup(int requestID, String remoteObjectName) throws IOException{
		
		byte[] remoteObjectInterface = Utils.objectToBytes(lookupTable.getRemoteBinding(remoteObjectName).getClass().getInterfaces());		

		ByteBuffer bb = ByteBuffer.allocate(1+4+remoteObjectInterface.length);
		bb.put(Statics.LOOKUP_RETURN_PACKET);
		bb.putInt(requestID);
		bb.put(remoteObjectInterface);
		
		endpoint.send(socketChannel,bb);

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
//		if (globalEndpointException!=null) throw globalEndpointException;

		final int requestID = endpoint.generateRequestID();
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