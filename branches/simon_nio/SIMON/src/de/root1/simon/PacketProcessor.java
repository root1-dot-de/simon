package de.root1.simon;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.root1.simon.utils.SimonClassLoader;
import de.root1.simon.utils.Utils;

/**
 * 
 * A inner class that reads and processes the packets
 * 
 * @author ACHR
 */
class PacketProcessor implements Runnable {

	private ByteBuffer packetHeader = ByteBuffer.allocate(5); // one byte + 4 byte integer containing the packet length
	private ByteBuffer packetBody; // the packet itself
	private int requestID = -1;
	private int msgType = -1;
	private int packetLength = -1;
	private String remoteObjectName;
	private final LookupTable lookupTable;
	private final Endpoint endpoint;
	private SocketChannel socketChannel;

	public PacketProcessor(LookupTable lookupTable, SocketChannel socketChannel, Endpoint endpoint) {
		Utils.debug("PacketProcessor.PacketProcessor() -> created for key="+socketChannel);
		this.socketChannel = socketChannel;
		this.lookupTable = lookupTable;
		this.endpoint = endpoint;
	}

	public void run() {
		Utils.debug("PacketProcessor.run() -> reading from: "+socketChannel);
//		socketChannel = (SocketChannel) key.channel();
		try {
			
			RxPacket p = new RxPacket(socketChannel);
			endpoint.removeFromReadFrom(socketChannel);			
			Utils.debug("PacketProcessor.run() -> msgType="+p.getMsgType());
			Utils.debug("PacketProcessor.run() -> requestID="+p.getRequestID());

			
			packetBody = p.getBody();
			
			if (Statics.DEBUG_MODE){
				byte[] b = packetBody.array();
				for (int i = 0; i < b.length; i++) {
					byte c = b[i];
					Utils.debug("PacketProcessor.run() -> body b["+i+"]="+c);
					
				}
			}
			
			
			// if the received data is a new request ...
			switch (msgType) {
				
				case Statics.INVOCATION_PACKET:
					Utils.debug("Endpoint.run() -> INVOCATION_PACKET -> start. requestID="+requestID);
					remoteObjectName = Utils.getString(packetBody);
					final long methodHash = packetBody.getLong();
					
					final Method method = lookupTable.getMethod(remoteObjectName, methodHash);
					final Class<?>[] parameterTypes = method.getParameterTypes();			
					final Object[] args = new Object[parameterTypes.length];
					
					// unwrapping the arguments
					for (int i = 0; i < args.length; i++) {
						args[i]=Utils.unwrapValue(parameterTypes[i], packetBody);							
					}
					
					Utils.debug("Endpoint.run() -> INVOCATION_PACKET -> remoteObject="+remoteObjectName+" methodHash="+methodHash+" method='"+method+"' args.length="+args.length);
					processInvokeMethod(remoteObjectName, method, args);
					Utils.debug("Endpoint.run() -> INVOCATION_PACKET -> end. requestID="+requestID);
					break;
					
				case Statics.LOOKUP_PACKET :
					processLookup(Utils.getString(packetBody));
					break;
					
				case Statics.TOSTRING_PACKET :
					processToString(Utils.getString(packetBody));
					break;
				
				case Statics.HASHCODE_PACKET :
					processHashCode(Utils.getString(packetBody));
					break;
				
				case Statics.EQUALS_PACKET :
					remoteObjectName = Utils.getString(packetBody);
					final Object object = Utils.getObject(packetBody);
					processEquals(remoteObjectName, object);
					break;	
					
				case Statics.INVOCATION_RETURN_PACKET :
					Utils.debug("Endpoint.run() -> INVOCATION_RETURN_PACKET -> start. requestID="+requestID);
					
					//unwrap the return-value
					Object result = Utils.unwrapValue(endpoint.removeRequestReturnType(requestID), packetBody);
					
					Utils.debug("Endpoint.run() -> INVOCATION_RETURN_PACKET -> requestID="+requestID+" result="+result);

					endpoint.putResultToQueue(requestID, result);
					endpoint.wakeWaitingProcess(requestID);
					
					Utils.debug("Endpoint.run() -> INVOCATION_RETURN_PACKET -> end. requestID="+requestID);
					break;
					
				case Statics.LOOKUP_RETURN_PACKET :
					endpoint.putResultToQueue(requestID, Utils.getObject(packetBody));
					endpoint.wakeWaitingProcess(requestID);
					break;
					
				case Statics.TOSTRING_RETURN_PACKET :
					endpoint.putResultToQueue(requestID, Utils.getString(packetBody));
					endpoint.wakeWaitingProcess(requestID);
					break;
				
				case Statics.HASHCODE_RETURN_PACKET :
					endpoint.putResultToQueue(requestID, packetBody.getInt());
					endpoint.wakeWaitingProcess(requestID);
					break;
					
				case Statics.EQUALS_RETURN_PACKET :
					endpoint.putResultToQueue(requestID, (Boolean.valueOf(packetBody.get()==1 ? true : false)));
					endpoint.wakeWaitingProcess(requestID);
					break;
					
				default :
//					interrupt();
//					globalEndpointException = new SimonRemoteException("invalid packet received from endpoint ...");
					try {
						socketChannel.close();
//						key.cancel();
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
	private void processHashCode(String remoteObjectName) throws IOException {
		
		final int hashcode = lookupTable.getRemoteBinding(remoteObjectName).hashCode();		
		
		ByteBuffer packet = ByteBuffer.allocate(1+4+4);
		packet.put(Statics.HASHCODE_RETURN_PACKET);
		packet.putInt(requestID);
		packet.putInt(hashcode);
		
		endpoint.send(socketChannel,packet);
		
	}
	
	/**
	 * 
	 *processes a request for a "toString()" call on a remote-object
	 * 
	 * @param requestID
	 * @param remoteObjectName
	 * @throws IOException
	 */
	private void processToString(String remoteObjectName) throws IOException {

		final String tostring = lookupTable.getRemoteBinding(remoteObjectName).toString();		
		
		ByteBuffer packet = ByteBuffer.allocate(1+4+(4+tostring.length()));
		packet.put(Statics.TOSTRING_RETURN_PACKET);
		packet.putInt(requestID);
		packet.put(Utils.stringToBytes(tostring));
		
		endpoint.send(socketChannel,packet);
		
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
	private void processEquals(String remoteObjectName, Object object) throws IOException{

		final boolean equals = lookupTable.getRemoteBinding(remoteObjectName).equals(object);		

		ByteBuffer packet = ByteBuffer.allocate(1+4+1);
		packet.put(Statics.EQUALS_RETURN_PACKET);
		packet.putInt(requestID);
		packet.put(equals ? (byte) 1 : (byte) 0);
		
		endpoint.send(socketChannel,packet);
		
	}
	
	/**
	 * 
	 * processes a lookup
	 * @throws IOException 
	 */
	private void processLookup(String remoteObjectName) throws IOException{
		Utils.debug("PacketProcessor.processLookup() -> start. requestID="+requestID);

		byte[] remoteObjectInterface = Utils.objectToBytes(lookupTable.getRemoteBinding(remoteObjectName).getClass().getInterfaces());		

		// create the data packet:
		// 1 byte  							-> packet type
		// 4 byte  							-> packet length
		// == 5 bytes header
		// 4 bytes 							-> request ID
		// 4 bytes objects length + object 	-> object 
		int packetLength = 4+remoteObjectInterface.length;
		ByteBuffer packet = ByteBuffer.allocate(5+packetLength);
				
		packet.put(Statics.LOOKUP_RETURN_PACKET);	// packet type
		packet.putInt(packetLength);				// packet length
		packet.putInt(requestID);					// request id
		packet.put(remoteObjectInterface);			// object
		
		endpoint.send(socketChannel,packet);
		Utils.debug("PacketProcessor.processLookup() -> end. requestID="+requestID);
	}
	
	private void processInvokeMethod(String remoteObjectName, Method method, Object[] args){
		Utils.debug("PacketProcessor.processInvokeMethod() -> begin. requestID="+requestID);
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
				Utils.debug("PacketProcessor.processInvokeMethod() -> start invoking method='"+method+"'. requestID="+requestID);
				result = method.invoke(endpoint.getLookupTable().getRemoteBinding(remoteObjectName), args);
				Utils.debug("PacketProcessor.processInvokeMethod() -> end invoking method='"+method+"'. requestID="+requestID+" result="+result);				
				// Search for SimonRemote in result
				if (result instanceof SimonRemote){
					endpoint.getLookupTable().putRemoteBinding(result.toString(), (SimonRemote)result);
					result = new SimonCallback((SimonRemote)result);
				}
				
			} catch (InvocationTargetException e){
				result = e.getTargetException();
			} 
			
			ByteBuffer packet = ByteBuffer.allocate(4096);
			// FIXME größe des pakets muss an index 1 "eingepflanzt" werden.
			packet.put(Statics.INVOCATION_RETURN_PACKET);
			packet.putInt(requestID);
			packet = Utils.wrapValue(method.getReturnType(), result, packet); // wrap the result
			
			endpoint.send(socketChannel, packet);
				
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
		Utils.debug("PacketProcessor.processInvokeMethod() -> end. requestID="+requestID);
	}
	
	
	
}