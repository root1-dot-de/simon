/**
 * TODO Documentation to be done
 */
package de.root1.simon;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.root1.simon.utils.Utils;

/**
 * TODO Documentation to be done
 *
 * @author achristian
 *
 */
public class Client {
	
	private int port = 2000;
	LookupTable lookupTable;
	Dispatcher dispatcher;
	private SocketChannel clientSocketChannel;
	private Selector selector;
	private SelectionKey key;
	private int requestIdCounter;
	
	/**
	 * TODO Documentation to be done
	 * @throws IOException 
	 */
	public Client() throws IOException {
		ExecutorService threadPool = Executors.newCachedThreadPool();
		lookupTable = new LookupTable();
		dispatcher = new Dispatcher(lookupTable,threadPool);
		
				
		
		Thread t = new Thread(dispatcher);
		t.start();
		
		connect();
	}
	
	public void connect() throws IOException{
		
		selector = SelectorProvider.provider().openSelector();
		Utils.debug("Client.connect() -> start");
		clientSocketChannel = SocketChannel.open();
		clientSocketChannel.configureBlocking(false);
	
		// Kick off connection establishment
		clientSocketChannel.connect(new InetSocketAddress("127.0.0.1", this.port));
	
		// Queue a channel registration since the caller is not the 
		// selecting thread. As part of the registration we'll register
		// an interest in connection events. These are raised when a channel
		// is ready to complete connection establishment.
		
		SelectionKey clientKey = clientSocketChannel.register(selector, SelectionKey.OP_CONNECT);
		
		selector.select();
		
		Iterator<SelectionKey> selectedKeys = this.selector.selectedKeys().iterator();
		while (selectedKeys.hasNext()) {
			key = (SelectionKey) selectedKeys.next();
			selectedKeys.remove();
			
			if (key.isConnectable()){
				
				SocketChannel socketChannel = (SocketChannel) key.channel();
				
				// Finish the connection. If the connection operation failed
				// this will raise an IOException.
				try {
					Utils.debug("Client.connect() -> finishing connection");
					socketChannel.finishConnect();
					Utils.debug("Client.connect() -> register on dispatcher");
					dispatcher.registerChannel(socketChannel);
					
				} catch (IOException e) {
					// Cancel the channel's registration with our selector
					System.err.println("Exception in finishConnection(): "+e);
					key.cancel();
					return;
				}
				
			} else throw new IllegalStateException("invalid op event: op="+key.interestOps());
			
		}
		
		Utils.debug("Client.connect() -> end");
	}
	
	/**
	 * 
	 * Generates a request ID
	 * 
	 * @return a request ID
	 */
	Integer generateRequestID() {
		return requestIdCounter++;
	}
	
	
	
	/**
	 * 
	 * TODO: Documentation to be done for method 'sendLookup', by 'ACHR'..
	 * 
	 * @param remoteObjectName
	 * @return the object we made the lookup for
	 * @throws SimonRemoteException 
	 * @throws IOException 
	 */
	public Object invokeLookup(String remoteObjectName) throws SimonRemoteException, IOException {
		final int requestID = generateRequestID(); 
		Utils.debug("Client.invokeLookup() -> start for requestID="+requestID);
//		if (globalEndpointException!=null) throw globalEndpointException;

 		// create a monitor that waits for the request-result
		final Object monitor = dispatcher.createMonitor(requestID);
		
		byte[] remoteObject = Utils.stringToBytes(remoteObjectName);
		
		TxPacket p = new TxPacket();
		p.setHeader(Statics.LOOKUP_PACKET, requestID);
		p.put(remoteObject);
		p.setComplete();
		ByteBuffer packet = p.getByteBuffer();
		
		
		// send the packet to the connected client-socket-channel
		dispatcher.send(key, packet);	
		Utils.debug("Client.invokeLookup() -> data send. waiting for answer for requestID="+requestID);
		// got to sleep until result is present
		synchronized (monitor) {
			try {
				monitor.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
			
		Utils.debug("Dispatcher.invokeLookup() -> got answer for requestID="+requestID);
			
//		// check if there was an error while sleeping
//		if (globalEndpointException!=null) throw globalEndpointException;
		
		// get result
		Utils.debug("Dispatcher.invokeLookup() -> end. requestID="+requestID);
		return dispatcher.getResult(requestID);			
	}	
	
	
	
	/**
	 * sends a requested invocation to the server
	 * 
	 * @param remoteObjectName
	 * @param methodName
	 * @param parameterTypes
	 * @param args
	 * @param returnType 
	 * @return the method's result
	 * @throws SimonRemoteException if there's a problem with the communication
	 * @throws IOException 
	 */	 
// 	protected Object invokeMethod(String remoteObjectName, long methodHash, Class<?>[] parameterTypes, Object[] args, Class<?> returnType) throws SimonRemoteException, IOException {
// 		final int requestID = generateRequestID();
// 		Utils.debug("Dispatcher.sendInvocationToRemote() -> begin. requestID="+requestID);
//
// 		if (globalEndpointException!=null) throw globalEndpointException;
//
// 		// create a monitor that waits for the request-result
//		final Object monitor = createMonitor(requestID);
//		
//		// memory the return-type for later unwrap
//		requestReturnType.put(requestID, returnType);
//		
//		// register callback objects in the lookup-table
//		if (args != null) {
//			for (int i = 0; i < args.length; i++) {
//				if (args[i] instanceof SimonRemote) {
//					SimonCallback sc = new SimonCallback((SimonRemote)args[i]);
//					lookupTable.putRemoteBinding(sc.getId(), (SimonRemote)args[i]);
//					args[i] = sc; // overwrite arg with wrapped callback-interface
//				}
//			}
//		}
//
////		FIXME this was used for caching. now it's useless
////		sendCounter++;
////		if (sendCounter==Integer.MAX_VALUE) sendCounter=0;
//
//		// here we have to allocate more, because atm we don't know big the parameters are 
//		
//		TxPacket packet = new TxPacket();
//		packet.setHeader(Statics.INVOCATION_PACKET, requestID);
//		
//		packet.put(Utils.stringToBytes(remoteObjectName));
//		packet.putLong(methodHash);
//		
//		for (int i = 0; i < parameterTypes.length; i++) {
//            Utils.wrapValue(parameterTypes[i], args[i], packet);
//        }
//		
//		packet.setComplete();
//		
////		send(clientSocketChannel, packet);
//		dispatcher.send(key, packet.getByteBuffer());
//				
////		FIXME this was used for caching. now it's useless
////		objectOutputStream.flush();
////		if (sendCounter%objectCacheLifetime==0){
////			objectOutputStream.reset();
////		}
//				
//		// got to sleep until result is present
//		synchronized (monitor) {
//			try {
//				monitor.wait();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//		Utils.debug("Dispatcher.sendInvocationToRemote() -> end. requestID="+requestID);
//		return requestResults.remove(requestID);
//	}
//
//	
//	/**
//	 * 
//	 * TODO: Documentation to be done for method 'sendLookup', by 'ACHR'..
//	 * 
//	 * @param remoteObjectName
//	 * @return
//	 * @throws IOException 
//	 */
//	protected String invokeToString(String remoteObjectName) throws IOException {
//		if (globalEndpointException!=null) throw globalEndpointException;
//
//		final int requestID = generateRequestID();
//
//		// create a monitor that waits for the request-result
//		final Object monitor = createMonitor(requestID);
//
//		// --
//		// create the data packet:
//		// 1 byte  					-> packet type
//		// 4 bytes 					-> request ID
//		// 4 bytes + string.length 	-> String length as integer + following string
//		ByteBuffer packet = ByteBuffer.allocate(1+4+(4+remoteObjectName.length()));
//		
//		// put the data in the packet
//		packet.put(Statics.TOSTRING_PACKET); 		// msg type
//		packet.putInt(requestID); 				// requestid
//		packet.put(Utils.stringToBytes(remoteObjectName)); 	// name of remote object in lookuptable	
//		
//		// send the packet to the connected client-socket-channel
////		send(clientSocketChannel, packet);
//		send(clientKey, packet);
//		
//
//		// got to sleep until result is present
//		try {
//			monitor.wait();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//			
//			
//		// check if there was an error while sleeping
//		if (globalEndpointException!=null) throw globalEndpointException;
//		
//		// get result
//		synchronized (requestResults) {
//			return (String)requestResults.remove(requestID);			
//		}		
//	}
//
//	
//	/**
//	 * 
//	 * TODO: Documentation to be done for method 'sendLookup', by 'ACHR'..
//	 * 
//	 * @param remoteObjectName
//	 * @return
//	 * @throws IOException 
//	 */
//	protected int invokeHashCode(String remoteObjectName) throws IOException {
//		if (globalEndpointException!=null) throw globalEndpointException;
//
//		final int requestID = generateRequestID();
//		
//		// create a monitor that waits for the request-result
//		final Object monitor = createMonitor(requestID);
//
//		// create the data packet:
//		// 1 byte  					-> packet type
//		// 4 bytes 					-> request ID
//		// 4 bytes + string.length 	-> String length as integer + following string
//		ByteBuffer packet = ByteBuffer.allocate(1+4+(4+remoteObjectName.length()));
//		
//		// put the data in the packet
//		packet.put(Statics.HASHCODE_PACKET); 		// msg type
//		packet.putInt(requestID); 				// requestid
//		packet.put(Utils.stringToBytes(remoteObjectName)); 	// name of remote object in lookuptable	
//		
//		// send the packet to the connected client-socket-channel
////		send(clientSocketChannel, packet);
//		send(clientKey, packet);
//		
//
//		// got to sleep until result is present
//		try {
//			monitor.wait();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//			
//			
//		// check if there was an error while sleeping
//		if (globalEndpointException!=null) throw globalEndpointException;
//		
//		// get result
//		synchronized (requestResults) {
//			return (Integer)requestResults.remove(requestID);			
//		}		
//	}
//
//
//	protected boolean invokeEquals(String remoteObjectName, Object object) throws IOException {
//		if (globalEndpointException!=null) throw globalEndpointException;
//
//		final int requestID = generateRequestID();
//		
//		// create a monitor that waits for the request-result
//		final Object monitor = createMonitor(requestID);
//
//		ByteBuffer packet = ByteBuffer.allocate(4096);
//
//		packet.put(Statics.EQUALS_PACKET);
//		packet.putInt(requestID);
//		packet.put(Utils.stringToBytes(remoteObjectName));
//		packet.put(Utils.objectToBytes(object));
//		
////		send(clientSocketChannel, packet);
//		send(clientKey, packet);
//		
//		// got to sleep until result is present
//		try {
//			monitor.wait();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//			
//			
//		// check if there was an error while sleeping
//		if (globalEndpointException!=null) throw globalEndpointException;
//		
//		// get result
//		return (Boolean) requestResults.remove(requestID);
//	}

	
	public static void main(String[] args) throws IOException {
		
		Statics.DEBUG_MODE = true;
		
		Client client = new Client();
		client.invokeLookup("server");
		
	}

}
