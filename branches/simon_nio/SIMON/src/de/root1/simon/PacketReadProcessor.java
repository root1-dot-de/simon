package de.root1.simon;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import de.root1.simon.utils.Utils;

/**
 * 
 * A inner class that reads and processes the packets
 * 
 * @author ACHR
 */
class PacketReadProcessor implements Runnable {

	private SelectionKey key = null;
	private ByteBuffer bbHeader = ByteBuffer.allocate(5); // one byte + 4 byte integer
	private ByteBuffer bbPacket; // one byte + 4 byte integer
	private int requestID = -1;
	private int msgType = -1;
	private int packetLength = -1;
	private String remoteObjectName;
	private LookupTable lookupTable;

	public PacketReadProcessor(LookupTable lookupTable, SelectionKey key) {
		this.key = key;
		this.lookupTable = lookupTable;
	}

	public void run() {
		
		SocketChannel socketChannel = (SocketChannel) key.channel();
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
					Simon.getThreadPool().execute(new ProcessMethodInvocationRunnable(this,requestID, remoteObjectName, method, args));
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
//					interrupt();
					globalEndpointException = new SimonRemoteException("invalid packet received from endpoint ...");
					try {
						objectInputStream.close();
						objectOutputStream.close();
					} catch (IOException e) {
					}
					break;
			}

			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}