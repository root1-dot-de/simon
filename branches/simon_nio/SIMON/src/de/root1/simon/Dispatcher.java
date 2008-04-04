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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.root1.simon.utils.Utils;

/**
 * An endpoint represents one end of the socket-connection between client and server.
 * 
 * @author ACHR
 */
public class Dispatcher implements Runnable {

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

	/** internal counter for clearing object*stream cache */
	private int sendCounter = 0;
	
	/** the number of method calls after which the object*stream cache is cleared */
	private int objectCacheLifetime;
	
	// -----------------------
	// NIO STUFF
	
	// The selector we'll be monitoring. This contains alle the connected channels/clients
	private Selector selector;
	
	// A list of PendingChange instances
	private List<ChangeRequest> pendingChanges = new LinkedList<ChangeRequest>();
	
	// Maps a SocketChannel to a list of ByteBuffer instances
	private Map<SocketChannel, List<ByteBuffer>> pendingData = new HashMap<SocketChannel, List<ByteBuffer>>();
	
//	// The buffer into which we'll read data when it's available
//	private ByteBuffer readBuffer = ByteBuffer.allocate(8192);
	
	// The channel on which we'll accept connections
	private ServerSocketChannel serverSocketChannel;
	
	// this channel is used by the client-endpoint for sending data to the server 
	private SocketChannel clientSocketChannel;
	
	private int port = 0;
	
	private ExecutorService eventHandlerPool = null;
	private ExecutorService invocationPool = null;

	private List<SocketChannel> readFromKey = new ArrayList<SocketChannel>();

	private SelectionKey clientKey;

	

	/**
	 * 
	 * Creates a new so called <i>Endpoint</i>.  
	 * @param objectCacheLifetime 
	 * 
	 * @param lookupTable the reference to an instance of <code>LookupTable</code>
	 * @param threadName used for naming the <code>Endpoint</code>-thread.
	 * @throws IOException 
	 * @throws IOException 
	 */
	public Dispatcher(int objectCacheLifetime, LookupTable lookupTable, String threadName, boolean isServer, int port) throws IOException {
		Utils.debug("Dispatcher.Endpoint() -> start");
		if (objectCacheLifetime<1) throw new IllegalArgumentException("objectCacheLifetime must be >=1");
		
		// FIXME set the name of the thread?!
//		this.setName("Endpoint: "+threadName);
		this.lookupTable = lookupTable;
		this.port = port;
		
		// FIXME should be configurable
		eventHandlerPool = Executors.newSingleThreadExecutor(new NamedThreadPoolFactory("EventHandler"));
		invocationPool = Executors.newSingleThreadExecutor();
		
		this.objectCacheLifetime = objectCacheLifetime;
		if (isServer) {
			Utils.debug("Dispatcher.Endpoint() -> initSelectorServer()");
			this.selector = initSelectorServer();
			
		} else {
			Utils.debug("Dispatcher.Endpoint() -> initSelectorClient()");
			this.selector = initSelectorClient();
			connectToServer();
			
		}

		// run the local thread
		// FIXME has to be done by the pool or the acceptor.
		//start();
		
		Utils.debug("Dispatcher.Endpoint() -> end");
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
	
	/*
	 * Creating the selector and server channel
	 * The astute will have noticed the call to initSelector() in the constructor. 
	 * Needless to say this method doesn't exist yet. So let's write it. It's job 
	 * is to create and initialize a non-blocking server channel and a selector. 
	 * It must and then register the server channel with that selector. 
	 */
	private Selector initSelectorServer() throws IOException {
		// Create a new selector
		Selector socketSelector = SelectorProvider.provider().openSelector();

		// Create a new non-blocking server socket channel
		this.serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.configureBlocking(false);

		// Bind the server socket to the specified address and port
		InetSocketAddress isa = new InetSocketAddress("0.0.0.0", port);
		serverSocketChannel.socket().bind(isa);

		// Register the server socket channel, indicating an interest in 
		// accepting new connections
		serverSocketChannel.register(socketSelector, SelectionKey.OP_ACCEPT);

		return socketSelector;
	}
	
	private Selector initSelectorClient() throws IOException {
		// Create a new selector
		return SelectorProvider.provider().openSelector();
	}
	
	/*
	 * Accepting connections
	 * Right. At this point we have a server socket channel ready and waiting and we've 
	 * indicated that we'd like to know when a new connection is available to be accepted.
	 * Now we need to actually accept it. Which brings us to our "select loop". This is 
	 * where most of the action kicks off. In a nutshell our selecting thread sits in a 
	 * loop waiting until one of the channels registered with the selector is in a state 
	 * that matches the interest operations we've registered for it. In this case the 
	 * operation we're waiting for on the server socket channel is an accept 
	 * (indicated by OP_ACCEPT). So let's take a look at the first iteration 
	 * (I couldn't resist) of our run() method. 
	 */
	private void accept(SelectionKey key) throws IOException {
		Utils.debug("Dispatcher.accept(): Start");
		// For an accept to be pending the channel must be a server socket channel.
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

		// Accept the connection and make it non-blocking
		SocketChannel socketChannel = serverSocketChannel.accept();
		Socket socket = socketChannel.socket();
		Utils.debug("Dispatcher.accept(): Client: "+socket.getInetAddress());
		socketChannel.configureBlocking(false);

		socketChannel.register(selector, SelectionKey.OP_READ);
		Utils.debug("Dispatcher.accept(): End");
	}
	
	/**
	 * The main receive-loop
	 * @see java.lang.Thread#run()
	 */
	public void run() {
//		super.run();
		
		while (true) {
			Utils.debug("");
			Utils.debug("");
			try {
				// Process any pending changes
				// this means read/write interests on channels
				synchronized (this.pendingChanges) {
					Iterator<ChangeRequest> changes = this.pendingChanges.iterator();
					while (changes.hasNext()) {
						ChangeRequest change = (ChangeRequest) changes.next();
						Utils.debug("Dispatcher.run() -> changeRequest -> "+change);
						switch (change.type) {

							case ChangeRequest.CHANGEOPS:
								SelectionKey key = change.socket.keyFor(this.selector);
								key.interestOps(change.ops);

							case ChangeRequest.REGISTER:
								change.socket.register(this.selector, change.ops);
								break;
								
						}
					}
					this.pendingChanges.clear();
				}
				// -------------

				Utils.debug("Dispatcher.run() -> selector.select() -> Wait for an event one of the registered channels");
				int numOfselectableKeys = selector.select();

				if (numOfselectableKeys>0) {

					Utils.debug("Dispatcher.run() -> "+numOfselectableKeys+" keys available");

					// Iterate over the set of keys for which events are available
					Iterator<SelectionKey> selectedKeys = this.selector.selectedKeys().iterator();
					while (selectedKeys.hasNext()) {
					
						SelectionKey key = (SelectionKey) selectedKeys.next();
						selectedKeys.remove();
						
						Utils.debug("Dispatcher.run() -> key has ready Ops: "+key.readyOps());
						
						if (!key.isValid()) {
							Utils.debug("Dispatcher.run() -> key is invalid!");
							continue; // .. with next in "while"
						}
	
						// Check what event is available and deal with it
						if (key.isAcceptable()){ // used by the server
							
							Utils.debug("Dispatcher.run() -> "+key+" is acceptable");
							accept(key);
							
						} else if (key.isConnectable()) { // used by the client
							
							Utils.debug("Dispatcher.run() -> "+key+" is connectable");
							this.finishConnection(key);
							
						} else if (key.isReadable()) {

							SocketChannel socketChannel =(SocketChannel) key.channel();
							
							RxPacket p = new RxPacket(socketChannel);
							
							int msgType = p.getMsgType();
							int requestID = p.getRequestID();
							ByteBuffer packetBody = p.getBody();
							
							Utils.debug("Dispatcher.run() -> "+key+" is readable");
							eventHandlerPool.execute(new EventHandler(key,this,p));
							
						} else if (key.isWritable()) {
							
							Utils.debug("Dispatcher.run() -> "+key+" is writeable");
							this.write(key);
							
						}
					}

				} else {
				
					Utils.debug("Dispatcher.run() -> no keys available -> 0 ");
					
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				// FIXME Exception richtig fangen
				wakeAllMonitors();
			}
		}
	}
	
	/**
	 * 
	 * Finish clients connection to the server
	 * @param key the client's <code>SelectionKey</code>
	 * @throws IOException
	 */
	private void finishConnection(SelectionKey key) throws IOException {
		Utils.debug("Dispatcher.finishConnection() -> start");
		SocketChannel socketChannel = (SocketChannel) key.channel();
	
		// Finish the connection. If the connection operation failed
		// this will raise an IOException.
		try {
			socketChannel.finishConnect();
		} catch (IOException e) {
			// Cancel the channel's registration with our selector
			System.err.println("Exception in finishConnection(): "+e);
			key.cancel();
			return;
		}
	
		Utils.debug("Dispatcher.finishConnection() -> register for read-mode");
		key.interestOps(SelectionKey.OP_READ);
		Utils.debug("Dispatcher.finishConnection() -> end");
	}
	
	
	private void connectToServer() throws IOException {
		Utils.debug("Dispatcher.connectToServer() -> start");
		clientSocketChannel = SocketChannel.open();
		clientSocketChannel.configureBlocking(false);
	
		// Kick off connection establishment
		clientSocketChannel.connect(new InetSocketAddress("127.0.0.1", this.port));
	
		// Queue a channel registration since the caller is not the 
		// selecting thread. As part of the registration we'll register
		// an interest in connection events. These are raised when a channel
		// is ready to complete connection establishment.
//		synchronized(this.pendingChanges) {
//			this.pendingChanges.add(new ChangeRequest(clientSocketChannel, ChangeRequest.REGISTER, SelectionKey.OP_CONNECT));
//		}
		
		clientKey = clientSocketChannel.register(selector, SelectionKey.OP_CONNECT);
		Utils.debug("Dispatcher.connectToServer() -> end");
	}
	
	/**
	 * Is called from the run() loop
	 *
	 * @param key
	 * @throws IOException
	 */
	private void write(SelectionKey key) throws IOException {
		Utils.debug("Dispatcher.write() -> start");
		SocketChannel socketChannel = (SocketChannel) key.channel();
		Utils.debug("Dispatcher.write() -> sende daten für key="+key+" channel="+socketChannel);
	    synchronized (this.pendingData) {
	      List queue = (List) this.pendingData.get(socketChannel);
	      
	      // Write until there's not more data ...
	      while (!queue.isEmpty()) {
	        ByteBuffer buf = (ByteBuffer) queue.get(0);
	        
	        if (Statics.DEBUG_MODE){
				byte[] b = buf.array();
				for (int i = 9; i < buf.limit(); i++) {
					byte c = b[i];
					Utils.debug("Dispatcher.write() -> body b["+(i-9)+"]="+c);
					
				}
			}
	        
	        socketChannel.write(buf);
	        if (buf.remaining() > 0) {
	          // ... or the socket's buffer fills up
	          break;
	        }
	        queue.remove(0);
	      }
	      
	      if (queue.isEmpty()) {
	        // We wrote away all data, so we're no longer interested
	        // in writing on this socket. Switch back to waiting for
	        // data.
	        key.interestOps(SelectionKey.OP_READ);
	      }
	    }
		Utils.debug("Dispatcher.write() -> end");
	}
	
	/**
	 * Sends the data to the socketchannel
	 * Be warned: only the data from start to position is sent
	 */
	protected void send(SelectionKey key, ByteBuffer packet) {
		
		SocketChannel socketChannel = (SocketChannel) key.channel();
		Utils.debug("Dispatcher.send() -> start");			
		Utils.debug("Dispatcher.send() -> sende daten für key="+key+" channel="+socketChannel);
		synchronized (this.pendingChanges) {
			// Indicate we want the interest ops set changed
			this.pendingChanges.add(new ChangeRequest(socketChannel, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));

			// And queue the data we want written
			synchronized (this.pendingData) {
				List<ByteBuffer> queue = this.pendingData.get(socketChannel);
				if (queue == null) {
					queue = new ArrayList<ByteBuffer>();
					this.pendingData.put(socketChannel, queue);
				}
				Utils.debug("Dispatcher.send() -> added packet for socketChannel="+socketChannel+" with limit="+packet.limit()+" to queue");
				queue.add(packet);
//				try {
//					Thread.sleep(10000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
			}
		}

		// Finally, wake up our selecting thread so it can make the required changes
		this.selector.wakeup();
		Utils.debug("Dispatcher.send() -> end");
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
		Utils.debug("Dispatcher.invokeLookup() -> start for requestID="+requestID);
		if (globalEndpointException!=null) throw globalEndpointException;

 		// create a monitor that waits for the request-result
		final Object monitor = createMonitor(requestID);
		
		byte[] remoteObject = Utils.stringToBytes(remoteObjectName);
		
		TxPacket p = new TxPacket();
		p.setHeader(Statics.LOOKUP_PACKET, requestID);
		p.put(remoteObject);
		p.setComplete();
		ByteBuffer packet = p.getByteBuffer();
		
		// create the data packet:
		// 1 byte  					-> packet type
		// 4 byte  					-> packet length
		// 4 bytes 					-> request ID
		// 4 bytes + string.length 	-> String length as integer + following string
//		int packetLength = 4+(4+remoteObjectName.length());
//		ByteBuffer packet = ByteBuffer.allocate(5+packetLength);
//		
//		// put the data in the packet
//		packet.put(Statics.LOOKUP_PACKET); 		// msg type
//		packet.putInt(packetLength); 			// packet length
//		packet.putInt(requestID); 				// requestid
//		packet.put(Utils.stringToBytes(remoteObjectName)); 	// name of remote object in lookuptable	

		
		// send the packet to the connected client-socket-channel
//		send(clientSocketChannel, packet);
		send(clientKey, packet);	
		Utils.debug("Dispatcher.invokeLookup() -> data send. waiting for answer for requestID="+requestID);
		// got to sleep until result is present
		synchronized (monitor) {
			try {
				monitor.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
			
		Utils.debug("Dispatcher.invokeLookup() -> got answer for requestID="+requestID);
			
		// check if there was an error while sleeping
		if (globalEndpointException!=null) throw globalEndpointException;
		
		// get result
		synchronized (requestResults) {
			Utils.debug("Dispatcher.invokeLookup() -> end. requestID="+requestID);
			return requestResults.remove(requestID);			
		}
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
 	protected Object invokeMethod(String remoteObjectName, long methodHash, Class<?>[] parameterTypes, Object[] args, Class<?> returnType) throws SimonRemoteException, IOException {
 		final int requestID = generateRequestID();
 		Utils.debug("Dispatcher.sendInvocationToRemote() -> begin. requestID="+requestID);

 		if (globalEndpointException!=null) throw globalEndpointException;

 		// create a monitor that waits for the request-result
		final Object monitor = createMonitor(requestID);
		
		// memory the return-type for later unwrap
		requestReturnType.put(requestID, returnType);
		
		// register callback objects in the lookup-table
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				if (args[i] instanceof SimonRemote) {
					SimonCallback sc = new SimonCallback((SimonRemote)args[i]);
					lookupTable.putRemoteBinding(sc.getId(), (SimonRemote)args[i]);
					args[i] = sc; // overwrite arg with wrapped callback-interface
				}
			}
		}

//		FIXME this was used for caching. now it's useless
//		sendCounter++;
//		if (sendCounter==Integer.MAX_VALUE) sendCounter=0;

		// here we have to allocate more, because atm we don't know big the parameters are 
		
		TxPacket packet = new TxPacket();
		packet.setHeader(Statics.INVOCATION_PACKET, requestID);
		
		packet.put(Utils.stringToBytes(remoteObjectName));
		packet.putLong(methodHash);
		
		for (int i = 0; i < parameterTypes.length; i++) {
            Utils.wrapValue(parameterTypes[i], args[i], packet);
        }
		
		packet.setComplete();
		
//		send(clientSocketChannel, packet);
		send(clientKey, packet.getByteBuffer());
				
//		FIXME this was used for caching. now it's useless
//		objectOutputStream.flush();
//		if (sendCounter%objectCacheLifetime==0){
//			objectOutputStream.reset();
//		}
				
		// got to sleep until result is present
		synchronized (monitor) {
			try {
				monitor.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Utils.debug("Dispatcher.sendInvocationToRemote() -> end. requestID="+requestID);
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

		// create a monitor that waits for the request-result
		final Object monitor = createMonitor(requestID);

		// --
		// create the data packet:
		// 1 byte  					-> packet type
		// 4 bytes 					-> request ID
		// 4 bytes + string.length 	-> String length as integer + following string
		ByteBuffer packet = ByteBuffer.allocate(1+4+(4+remoteObjectName.length()));
		
		// put the data in the packet
		packet.put(Statics.TOSTRING_PACKET); 		// msg type
		packet.putInt(requestID); 				// requestid
		packet.put(Utils.stringToBytes(remoteObjectName)); 	// name of remote object in lookuptable	
		
		// send the packet to the connected client-socket-channel
//		send(clientSocketChannel, packet);
		send(clientKey, packet);
		

		// got to sleep until result is present
		try {
			monitor.wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
			
			
		// check if there was an error while sleeping
		if (globalEndpointException!=null) throw globalEndpointException;
		
		// get result
		synchronized (requestResults) {
			return (String)requestResults.remove(requestID);			
		}		
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
		
		// create a monitor that waits for the request-result
		final Object monitor = createMonitor(requestID);

		// create the data packet:
		// 1 byte  					-> packet type
		// 4 bytes 					-> request ID
		// 4 bytes + string.length 	-> String length as integer + following string
		ByteBuffer packet = ByteBuffer.allocate(1+4+(4+remoteObjectName.length()));
		
		// put the data in the packet
		packet.put(Statics.HASHCODE_PACKET); 		// msg type
		packet.putInt(requestID); 				// requestid
		packet.put(Utils.stringToBytes(remoteObjectName)); 	// name of remote object in lookuptable	
		
		// send the packet to the connected client-socket-channel
//		send(clientSocketChannel, packet);
		send(clientKey, packet);
		

		// got to sleep until result is present
		try {
			monitor.wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
			
			
		// check if there was an error while sleeping
		if (globalEndpointException!=null) throw globalEndpointException;
		
		// get result
		synchronized (requestResults) {
			return (Integer)requestResults.remove(requestID);			
		}		
	}


	protected boolean invokeEquals(String remoteObjectName, Object object) throws IOException {
		if (globalEndpointException!=null) throw globalEndpointException;

		final int requestID = generateRequestID();
		
		// create a monitor that waits for the request-result
		final Object monitor = createMonitor(requestID);

		ByteBuffer packet = ByteBuffer.allocate(4096);

		packet.put(Statics.EQUALS_PACKET);
		packet.putInt(requestID);
		packet.put(Utils.stringToBytes(remoteObjectName));
		packet.put(Utils.objectToBytes(object));
		
//		send(clientSocketChannel, packet);
		send(clientKey, packet);
		
		// got to sleep until result is present
		try {
			monitor.wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
			
			
		// check if there was an error while sleeping
		if (globalEndpointException!=null) throw globalEndpointException;
		
		// get result
		return (Boolean) requestResults.remove(requestID);
	}

	/**
	 * Wake the process with the related requestID
	 * 
	 * @param requestID the process to wake  
	 */
	protected void wakeWaitingProcess(int requestID) {
		synchronized (idMonitorMap) {						
			final Object monitor = idMonitorMap.remove(requestID);
			if (monitor!=null) {
				synchronized (monitor) {
					monitor.notify(); // wake the waiting method
				}
			} else {
				Utils.debug("Dispatcher.wakeWaitingProcess() -> !!!!!!!!!!!!!!!! -> no monitor for requestID="+requestID+" idmonitormapsize="+idMonitorMap.size());
			}
			
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


	protected synchronized void putResultToQueue(int requestID, Object result){
		requestResults.put(requestID,result);
	}
	
	
	/**
	 * for internal use only
	 */
	protected LookupTable getLookupTable() {
		return lookupTable;
	}

	/**
	 * TODO Documentation to be done
	 * @return
	 */
	public ExecutorService getInvocationPool() {
		return invocationPool;
	}





	// FIXME reimplement asking for ip-address of client ...
//	/**
//	 * for internal use only
//	 * @return
//	 */
//	protected InetAddress getInetAddress() {
//		return socket.getInetAddress();
//	}
//	
//	/**
//	 * for internal use only
//	 * @return
//	 */
//	protected int getPort(){
//		return socket.getPort();
//	}
	
	
	
	/**
	 * 
	 * create a monitor that waits for the request-result that 
	 * is associated with the given request-id
	 * 
	 * @param requestID
	 * @return the monitor used for waiting for the result
	 */
	private Object createMonitor(final int requestID) {
		final Object monitor = new Object();
		synchronized (idMonitorMap) {
			Utils.debug("Dispatcher.createMonitor() -> monitor for requestID="+requestID+" -> monitor="+monitor+" mapsize="+idMonitorMap.size());
			idMonitorMap.put(requestID, monitor);
			Utils.debug("Dispatcher.createMonitor() -> monitor for requestID="+requestID+" new mapsize="+idMonitorMap.size());
		}
		return monitor;
	}

	protected synchronized Class<?> removeRequestReturnType(int requestID) {
		return requestReturnType.remove(requestID);
	}
	
	protected synchronized void removeFromReadFrom(SocketChannel channel){
		readFromKey.remove(channel);
	}




	

}
