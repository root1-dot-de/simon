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
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

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
//	private HashMap<Integer, Object> idMonitorMap = new HashMap<Integer, Object>();
	private HashMap<Integer, MonitorResult> idMonitorMap = new HashMap<Integer, MonitorResult>();
	
	/** The map that holds the relation between the request ID and the received result */
	private HashMap<Integer, Object> requestResults = new HashMap<Integer, Object>();
	
	/** a memory map for the client the unwrap the incoming return value after executing a method on the server */
	private HashMap<Integer, Class<?>> requestReturnType = new HashMap<Integer, Class<?>>();
	
	/** is used for exception between all the threads in here */
	private SimonRemoteException globalEndpointException = null;
	
	// -----------------------
	// NIO STUFF
	
	// The selector we'll be monitoring. This contains alle the connected channels/clients
	private Selector selector;
	
	// A list of PendingChange instances
	private List<ChangeRequest> pendingChanges = new LinkedList<ChangeRequest>();
	
	// Maps a SocketChannel to a list of ByteBuffer instances
	private Map<SocketChannel, List<ByteBuffer>> pendingData = new HashMap<SocketChannel, List<ByteBuffer>>();
	
	private ExecutorService eventHandlerPool = null;
	

	/**
	 * 
	 * Creates a new so called <i>Endpoint</i>.  
	 * @param objectCacheLifetime 
	 * 
	 * @param lookupTable the reference to an instance of <code>LookupTable</code>
	 * @param threadPool 
	 * @param threadName used for naming the <code>Endpoint</code>-thread.
	 * @throws IOException 
	 * @throws IOException 
	 */
	public Dispatcher(LookupTable lookupTable, ExecutorService threadPool) throws IOException {
		//Utils.debug("Dispatcher.Dispatcher() -> start");
		
		// FIXME set the name of the thread?!
//		this.setName("Endpoint: "+threadName);
		this.lookupTable = lookupTable;
		
		// FIXME should be configurable
		eventHandlerPool = threadPool;
		
		selector = initSelectorClient();
		
		//Utils.debug("Dispatcher.Dispatcher() -> end");
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
	
	
	private Selector initSelectorClient() throws IOException {
		// Create a new selector
		return SelectorProvider.provider().openSelector();
	}
	
	
	/**
	 * The main receive-loop
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		
		while (true) {
			//Utils.debug("");
			//Utils.debug("");
			try {
				// Process any pending selector changes
				// this means read/write interests and registrations
				synchronized (this.pendingChanges) {
					
					Iterator<ChangeRequest> changes = this.pendingChanges.iterator();
					while (changes.hasNext()) {
						
						ChangeRequest change = (ChangeRequest) changes.next();
						//Utils.debug("Dispatcher.run() -> changeRequest -> "+change);
						
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

				//Utils.debug("Dispatcher.run() -> selector.select() -> Wait for an event on one of the registered channels");
				int numOfselectableKeys = selector.select();

				if (numOfselectableKeys>0) {

					//Utils.debug("Dispatcher.run() -> "+numOfselectableKeys+" keys available");

					// Iterate over the set of keys for which events are available
					Iterator<SelectionKey> selectedKeys = this.selector.selectedKeys().iterator();
					while (selectedKeys.hasNext()) {
					
						SelectionKey key = (SelectionKey) selectedKeys.next();
						selectedKeys.remove();
						
						//Utils.debug("Dispatcher.run() -> key has ready op: "+Utils.printSelectionKeyValue(key.interestOps()));
						
						if (!key.isValid()) {
							//Utils.debug("Dispatcher.run() -> key is invalid!");
							continue; // .. with next in "while"
						}
	
						// Check what event is available and deal with it
						if (key.isAcceptable()){ // used by the server
							
							//Utils.debug("Dispatcher.run() -> "+key+" is acceptable. Accepting is done by the 'Acceptor'!");
							
						} else if (key.isConnectable()) { // used by the client
							
							//Utils.debug("Dispatcher.run() -> "+key+" is connectable.  FinishConnection is done by the 'Client'!");
							
						} else if (key.isReadable()) {

							//Utils.debug("Dispatcher.run() -> "+key+" is readable");
							key.interestOps(0); // deregister for read-events
							handleRead(key);
							
						} else if (key.isWritable()) {
							
							//Utils.debug("Dispatcher.run() -> "+key+" is writeable");
							key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE); // deregister for write events
							handleWrite(key);
							
						}
					}

				} else {
				
					//Utils.debug("Dispatcher.run() -> no keys available");
					
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				// FIXME Exception richtig fangen
				wakeAllMonitors();
			}
		}
	}



	private void handleRead(SelectionKey key) {
		eventHandlerPool.execute(new ReadEventHandler(key,this));
	}
	
	private void handleWrite(SelectionKey key) throws IOException {
		//Utils.debug("Dispatcher.handleWrite() -> start");
	    SocketChannel socketChannel = (SocketChannel) key.channel();

	    synchronized (this.pendingData) {
	      List<ByteBuffer> queue = (List<ByteBuffer>) this.pendingData.get(socketChannel);
	      
	      // Write until there's not more data ...
	      while (!queue.isEmpty()) {
	        ByteBuffer buf = (ByteBuffer) queue.get(0);
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
	    //Utils.debug("Dispatcher.handleWrite() -> end");
	}
	
	/**
	 * Sends the data to the socketchannel
	 * Be warned: only the data from start to position is sent
	 */
	protected void send(SelectionKey key, ByteBuffer packet) {
		
		SocketChannel socketChannel = (SocketChannel) key.channel();
		//Utils.debug("Dispatcher.send() -> start");			
		//Utils.debug("Dispatcher.send() -> sende daten für key="+key+" channel="+socketChannel);
		
//		packet.position(1);
//		int reqID = packet.getInt();
//		packet.rewind();
//		System.out.println(System.nanoTime()+" Dispatcher.send() -> sende reqID="+reqID);
		

		// And queue the data we want written
		synchronized (this.pendingData) {
			List<ByteBuffer> queue = this.pendingData.get(socketChannel);
			if (queue == null) {
				queue = new ArrayList<ByteBuffer>();
				this.pendingData.put(socketChannel, queue);
			}
			//Utils.debug("Dispatcher.send() -> added packet for socketChannel="+socketChannel+" with limit="+packet.limit()+" to queue");
			queue.add(packet);
		}

		selectorChangeRequest(socketChannel, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE);

		//Utils.debug("Dispatcher.send() -> end");
	}



	private void selectorChangeRequest(SocketChannel socketChannel, int type, int operation) {
		synchronized (this.pendingChanges) {
			// Indicate we want the interest ops set changed
			this.pendingChanges.add(new ChangeRequest(socketChannel, type, operation));
			selector.wakeup();
		}
		// Finally, wake up our selecting thread so it can make the required changes
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
	protected Object invokeLookup(SelectionKey key, String remoteObjectName) throws SimonRemoteException, IOException {
		final int requestID = generateRequestID(); 
		//Utils.debug("Dispatcher.invokeLookup() -> start for requestID="+requestID+" key="+key);
		if (globalEndpointException!=null) throw globalEndpointException;

 		// create a monitor that waits for the request-result
//		final Object monitor = createMonitor(requestID);
		final MonitorResult monitor = createMonitor(requestID);
		
		byte[] remoteObject = Utils.stringToBytes(remoteObjectName);
		
		TxPacket packet = new TxPacket();
		packet.setHeader(Statics.LOOKUP_PACKET, requestID);
		packet.put(remoteObject);
		packet.setComplete();
		
		// send the packet to the connected channel
		send(key, packet.getByteBuffer());	
		//Utils.debug("Dispatcher.invokeLookup() -> data send. waiting for answer for requestID="+requestID);
		
		// got to sleep until result is present
//		synchronized (monitor) {
			try {
//				monitor.wait();
				monitor.waitForResult();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
//		}
			
		//Utils.debug("Dispatcher.invokeLookup() -> got answer for requestID="+requestID);
			
		// check if there was an error while sleeping
		if (globalEndpointException!=null) throw globalEndpointException;
		
		// get result
		synchronized (requestResults) {
			//Utils.debug("Dispatcher.invokeLookup() -> end. requestID="+requestID);
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
 	protected Object invokeMethod(SelectionKey key, String remoteObjectName, long methodHash, Class<?>[] parameterTypes, Object[] args, Class<?> returnType) throws SimonRemoteException, IOException {
 		final int requestID = generateRequestID();
 		//Utils.debug("Dispatcher.sendInvocationToRemote() -> begin. requestID="+requestID+" key="+key);

 		if (globalEndpointException!=null) throw globalEndpointException;

 		// create a monitor that waits for the request-result
//		final Object monitor = createMonitor(requestID);
		final MonitorResult monitor = createMonitor(requestID);
		
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

		
		TxPacket packet = new TxPacket();
		packet.setHeader(Statics.INVOCATION_PACKET, requestID);
		packet.put(Utils.stringToBytes(remoteObjectName));
		packet.putLong(methodHash);
		
		for (int i = 0; i < parameterTypes.length; i++) {
            Utils.wrapValue(parameterTypes[i], args[i], packet);
        }
		
		packet.setComplete();
		send(key, packet.getByteBuffer());
				
		// got to sleep until result is present
//		synchronized (monitor) {
			try {
//				System.out.println(System.nanoTime()+" Dispatcher invoke method. monitor="+monitor+" Wait for result for reqID="+requestID);
//				monitor.wait();
				monitor.waitForResult();
//				System.out.println(System.nanoTime()+" Dispatcher invoke method. monitor="+monitor+" Wait for result for reqID="+requestID+" finished");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
//		}
		//Utils.debug("Dispatcher.sendInvocationToRemote() -> end. requestID="+requestID);
		synchronized (requestResults) {
			return requestResults.remove(requestID);
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
	protected String invokeToString(SelectionKey key, String remoteObjectName) throws IOException {
		if (globalEndpointException!=null) throw globalEndpointException;

		final int requestID = generateRequestID();

		// create a monitor that waits for the request-result
		final Object monitor = createMonitor(requestID);

		TxPacket packet = new TxPacket();
		packet.setHeader(Statics.TOSTRING_PACKET, requestID);
		packet.put(Utils.stringToBytes(remoteObjectName));
		packet.setComplete();
		
		
		// send the packet to the connected client-socket-channel
		send(key, packet.getByteBuffer());
		

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
	protected int invokeHashCode(SelectionKey key, String remoteObjectName) throws IOException {
		if (globalEndpointException!=null) throw globalEndpointException;

		final int requestID = generateRequestID();
		
		// create a monitor that waits for the request-result
		final Object monitor = createMonitor(requestID);

		TxPacket packet = new TxPacket();
		packet.setHeader(Statics.HASHCODE_PACKET, requestID);
		packet.put(Utils.stringToBytes(remoteObjectName));
		packet.setComplete();
		
		send(key, packet.getByteBuffer());
		

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


	protected boolean invokeEquals(SelectionKey key, String remoteObjectName, Object object) throws IOException {
		if (globalEndpointException!=null) throw globalEndpointException;

		final int requestID = generateRequestID();
		
		// create a monitor that waits for the request-result
		final Object monitor = createMonitor(requestID);

		TxPacket packet = new TxPacket();
		packet.setHeader(Statics.EQUALS_PACKET, requestID);
		packet.put(Utils.stringToBytes(remoteObjectName));
		packet.put(Utils.objectToBytes(object));
		packet.setComplete();
		
		send(key, packet.getByteBuffer());
		
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
//			System.out.println("waking id="+requestID);
//			final Object monitor = idMonitorMap.remove(requestID);
			final MonitorResult monitor = idMonitorMap.remove(requestID);
			if (monitor!=null) {
//				synchronized (monitor) {
//					monitor.notify(); // wake the waiting method
					monitor.wakeUp(); // wake the waiting method
//					System.out.println("id="+requestID+" monitor="+monitor+" waked");
//				}
			} else {
				//Utils.debug("Dispatcher.wakeWaitingProcess() -> !!!!!!!!!!!!!!!! -> no monitor for requestID="+requestID+" idmonitormapsize="+idMonitorMap.size());
//				System.out.println("Dispatcher.wakeWaitingProcess() -> !!!!!!!!!!!!!!!! -> no monitor for requestID="+requestID+" idmonitormapsize="+idMonitorMap.size());
			}
			
		}
	}

	/** 
	 * TODO should be placed outside the dispatcher?
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
	 * 
	 * create a monitor that waits for the request-result that 
	 * is associated with the given request-id
	 * 
	 * @param requestID
	 * @return the monitor used for waiting for the result
	 */
//	Object createMonitor(final int requestID) {
	private MonitorResult createMonitor(final int requestID) {
//		final Object monitor = new Object();
		final MonitorResult monitor = new MonitorResult();
		synchronized (idMonitorMap) {
			//Utils.debug("Dispatcher.createMonitor() -> monitor for requestID="+requestID+" -> monitor="+monitor+" mapsize="+idMonitorMap.size());
			idMonitorMap.put(requestID, monitor);
			//Utils.debug("Dispatcher.createMonitor() -> monitor for requestID="+requestID+" new mapsize="+idMonitorMap.size());
		}
		return monitor;
	}

	/**
	 * 
	 * TODO: Documentation to be done for method 'removeRequestReturnType', by 'ACHR'..
	 * 
	 * @param requestID
	 * @return
	 */
	protected synchronized Class<?> removeRequestReturnType(int requestID) {
		return requestReturnType.remove(requestID);
	}
	
	/**
	 * Registers a channel with the dispatcher's selector
	 * @param channel
	 * @throws ClosedChannelException 
	 */
	protected void registerChannel(SocketChannel channel) throws ClosedChannelException {
		//Utils.debug("Dispatcher.registerChannel() -> start");
		selectorChangeRequest(channel, ChangeRequest.REGISTER, SelectionKey.OP_READ);
		//Utils.debug("Dispatcher.registerChannel() -> end");
	}



	public void changeOpForReadiness(SocketChannel channel) {
		//Utils.debug("Dispatcher.registerChannel() -> start");
		selectorChangeRequest(channel, ChangeRequest.CHANGEOPS, SelectionKey.OP_READ);
		//Utils.debug("Dispatcher.registerChannel() -> end");
	}
	
	protected Object getResult(int requestID){
		synchronized (requestResults) {
			return requestResults.remove(requestID);			
		}
	}
	
}
