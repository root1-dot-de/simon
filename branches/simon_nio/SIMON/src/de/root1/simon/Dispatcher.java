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
import java.util.logging.Level;

import de.root1.simon.exceptions.SimonRemoteException;
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
//	private HashMap<Integer, MonitorResult> idMonitorMap = new HashMap<Integer, MonitorResult>();
	
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
		Utils.logger.fine("begin");
		
		// FIXME set the name of the thread?!
//		this.setName("Endpoint: "+threadName);
		this.lookupTable = lookupTable;
		
		// FIXME should be configurable
		eventHandlerPool = threadPool;
		
		selector = initSelectorClient();
		
		Utils.logger.fine("end");
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
	
	
	private Selector initSelectorClient() throws IOException {
		// Create a new selector
		return SelectorProvider.provider().openSelector();
	}
	
	
	/**
	 * The main receive-loop
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		Utils.logger.fine("begin");
		while (true) {
			try {
				// Process any pending selector changes
				// this means read/write interests and registrations
				synchronized (this.pendingChanges) {
					
					Iterator<ChangeRequest> changes = this.pendingChanges.iterator();
					while (changes.hasNext()) {
						
						ChangeRequest change = (ChangeRequest) changes.next();
						
						if (Utils.logger.isLoggable(Level.FINER))
							Utils.logger.finer("changerequest: "+change);
						
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

				Utils.logger.finer("Wait for an event on one of the registered channels");
				int numOfselectableKeys = selector.select();

				if (numOfselectableKeys>0) {

					if (Utils.logger.isLoggable(Level.FINER))
						Utils.logger.finer(numOfselectableKeys+ " keys ready");	
					
					// Iterate over the set of keys for which events are available
					Iterator<SelectionKey> selectedKeys = this.selector.selectedKeys().iterator();
					while (selectedKeys.hasNext()) {
					
						SelectionKey key = (SelectionKey) selectedKeys.next();
						selectedKeys.remove();
						
						if (Utils.logger.isLoggable(Level.FINER))
							Utils.logger.finer("key has ready op: "+Utils.printSelectionKeyValue(key.interestOps()));	
						
						if (!key.isValid()) {
							if (Utils.logger.isLoggable(Level.FINER))
								Utils.logger.finer("key is invalid: "+key);
							continue; // .. with next in "while"
						}
	
						// Check what event is available and deal with it
						if (key.isAcceptable()){ // used by the server
							if (Utils.logger.isLoggable(Level.FINER))
								Utils.logger.finer(key+" is acceptable. Accepting is done by the 'Acceptor'!");
							
						} else if (key.isConnectable()) { // used by the client

							if (Utils.logger.isLoggable(Level.FINER))
								Utils.logger.finer(key+" is connectable.  FinishConnection is done by the 'Client'!");
							
						} else if (key.isReadable()) {

							if (Utils.logger.isLoggable(Level.FINER))
								Utils.logger.finer(key+" is readable");
							
							key.interestOps(0); // deregister for read-events
							handleRead(key);
							
						} else if (key.isWritable()) {
							
							if (Utils.logger.isLoggable(Level.FINER))
								Utils.logger.finer(key+" is writeable");

							key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE); // deregister for write events
							handleWrite(key);
							
						}
					}

				} else {
				
						Utils.logger.finer("no keys available");
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				// FIXME Exception richtig fangen
				Utils.logger.severe("Exception: "+e);

				wakeAllMonitors();
			}
		}
	}



	private void handleRead(SelectionKey key) {
		Utils.logger.fine("begin");
		eventHandlerPool.execute(new ReadEventHandler(key,this));
		Utils.logger.fine("end");
	}
	
	private void handleWrite(SelectionKey key) throws IOException {
		Utils.logger.fine("begin");
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
	    Utils.logger.fine("end");	
	}
	
	/**
	 * Sends the data to the socketchannel
	 * Be warned: only the data from start to position is sent
	 */
	protected void send(SelectionKey key, ByteBuffer packet) {
		Utils.logger.fine("begin");
		
		SocketChannel socketChannel = (SocketChannel) key.channel();

		if (Utils.logger.isLoggable(Level.FINER))
			Utils.logger.finer("sending data for key="+key+" channel="+socketChannel);
		
		// And queue the data we want written
		synchronized (this.pendingData) {
			List<ByteBuffer> queue = this.pendingData.get(socketChannel);
			if (queue == null) {
				queue = new ArrayList<ByteBuffer>();
				this.pendingData.put(socketChannel, queue);
			}
			if (Utils.logger.isLoggable(Level.FINER))
				Utils.logger.finer("added packet for socketChannel="+socketChannel+" with limit="+packet.limit()+" to queue");
			queue.add(packet);
		}

		selectorChangeRequest(socketChannel, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE);

		Utils.logger.fine("end");
	}


	/**
	 * 
	 * TODO Documentation to be done
	 * @param socketChannel
	 * @param type
	 * @param operation
	 */
	private void selectorChangeRequest(SocketChannel socketChannel, int type, int operation) {
		Utils.logger.fine("begin");

		synchronized (this.pendingChanges) {
			// Indicate we want the interest ops set changed
			this.pendingChanges.add(new ChangeRequest(socketChannel, type, operation));
			// Finally, wake up our selecting thread so it can make the required changes
			selector.wakeup();
		}
		Utils.logger.fine("end");
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
		if (Utils.logger.isLoggable(Level.FINE))
			Utils.logger.fine("begin requestID="+requestID+" key="+key);

		if (globalEndpointException!=null) throw globalEndpointException;

 		// create a monitor that waits for the request-result
		final Object monitor = createMonitor(requestID);
		
		byte[] remoteObject = Utils.stringToBytes(remoteObjectName);
		
		TxPacket packet = new TxPacket();
		packet.setHeader(Statics.LOOKUP_PACKET, requestID);
		packet.put(remoteObject);
		packet.setComplete();
		
		// send the packet to the connected channel
		send(key, packet.getByteBuffer());	
		
		if (Utils.logger.isLoggable(Level.FINER))
			Utils.logger.finer("data send. waiting for answer for requestID="+requestID);
		
		// check if need to wait for the result
		synchronized (requestResults) {
			if (requestResults.containsKey(requestID))
				return requestResults.remove(requestID);
		}
		
		// got to sleep until result is present
		synchronized (monitor) {
			try {
				monitor.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
			
		if (Utils.logger.isLoggable(Level.FINER))
			Utils.logger.finer("got answer for requestID="+requestID);
			
		// check if there was an error while sleeping
		if (globalEndpointException!=null) throw globalEndpointException;
		
		// get result
		synchronized (requestResults) {
			if (Utils.logger.isLoggable(Level.FINE))
				Utils.logger.fine("end requestID="+requestID);
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

 		if (Utils.logger.isLoggable(Level.FINE))
 			Utils.logger.fine("begin. requestID="+requestID+" key="+key);
 		
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

		
		TxPacket packet = new TxPacket();
		packet.setHeader(Statics.INVOCATION_PACKET, requestID);
		packet.put(Utils.stringToBytes(remoteObjectName));
		packet.putLong(methodHash);
		
		for (int i = 0; i < parameterTypes.length; i++) {
            Utils.wrapValue(parameterTypes[i], args[i], packet);
        }
		
		packet.setComplete();
		synchronized (monitor) {
		send(key, packet.getByteBuffer());
		
//		System.out.println(System.nanoTime()+" data for reqid="+requestID+" sent");	
		// check if need to wait for the result
			synchronized (requestResults) {
				if (requestResults.containsKey(requestID))
					return requestResults.remove(requestID);
			}
		
			// got to sleep until result is present
			try {
				monitor.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		synchronized (requestResults) {
			if (Utils.logger.isLoggable(Level.FINE))
				Utils.logger.fine("end. requestID="+requestID);
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

		if (Utils.logger.isLoggable(Level.FINE))
 			Utils.logger.fine("begin. requestID="+requestID+" key="+key);
		
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
			if (Utils.logger.isLoggable(Level.FINE))
	 			Utils.logger.fine("end. requestID="+requestID);
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
		
		if (Utils.logger.isLoggable(Level.FINE))
 			Utils.logger.fine("begin. requestID="+requestID+" key="+key);
		
		// create a monitor that waits for the request-result
		final Object monitor = createMonitor(requestID);

		TxPacket packet = new TxPacket();
		packet.setHeader(Statics.HASHCODE_PACKET, requestID);
		packet.put(Utils.stringToBytes(remoteObjectName));
		packet.setComplete();
		
		send(key, packet.getByteBuffer());
		
		// check if need to wait for the result
		synchronized (requestResults) {
			if (requestResults.containsKey(requestID))
				return (Integer)requestResults.remove(requestID);
		}

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
			if (Utils.logger.isLoggable(Level.FINE))
	 			Utils.logger.fine("end. requestID="+requestID);
			return (Integer)requestResults.remove(requestID);			
		}		
	}


	/**
	 * 
	 * TODO Documentation to be done
	 * @param key
	 * @param remoteObjectName
	 * @param object
	 * @return
	 * @throws IOException
	 */
	protected boolean invokeEquals(SelectionKey key, String remoteObjectName, Object object) throws IOException {
		if (globalEndpointException!=null) throw globalEndpointException;

		final int requestID = generateRequestID();
		
		if (Utils.logger.isLoggable(Level.FINE))
 			Utils.logger.fine("begin. requestID="+requestID+" key="+key);
		
		// create a monitor that waits for the request-result
		final Object monitor = createMonitor(requestID);

		TxPacket packet = new TxPacket();
		packet.setHeader(Statics.EQUALS_PACKET, requestID);
		packet.put(Utils.stringToBytes(remoteObjectName));
		packet.put(Utils.objectToBytes(object));
		packet.setComplete();
		
		send(key, packet.getByteBuffer());
		// check if need to wait for the result
		synchronized (requestResults) {
			if (requestResults.containsKey(requestID))
				return (Boolean)requestResults.remove(requestID);
		}
		
		// got to sleep until result is present
		try {
			monitor.wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
			
			
		// check if there was an error while sleeping
		if (globalEndpointException!=null) throw globalEndpointException;
		
		if (Utils.logger.isLoggable(Level.FINE))
 			Utils.logger.fine("end. requestID="+requestID);
		// get result
		return (Boolean) requestResults.remove(requestID);
	}


	/**
	 * Wake the process with the related requestID
	 * 
	 * @param requestID the process to wake  
	 */
	protected void wakeWaitingProcess(int requestID) {
		if (Utils.logger.isLoggable(Level.FINE))
			Utils.logger.fine("begin. wakeing requestID="+requestID);
		synchronized (idMonitorMap) {
			final Object monitor = idMonitorMap.remove(requestID);
			if (monitor!=null) {
				synchronized (monitor) {
					monitor.notify(); // wake the waiting method
//					System.out.println("id="+requestID+" monitor="+monitor+" waked");
					if (Utils.logger.isLoggable(Level.FINER))
			 			Utils.logger.finer("id="+requestID+" monitor="+monitor+" waked");
				}
			} else {
				if (Utils.logger.isLoggable(Level.FINER))
		 			Utils.logger.finer("no monitor for requestID="+requestID+" idmonitormapsize="+idMonitorMap.size());
			}
			
		}
		if (Utils.logger.isLoggable(Level.FINE))
 			Utils.logger.fine("end. wakeing requestID="+requestID);
	}

	/** 
	 * TODO should be placed outside the dispatcher?
	 * wake all waiting processes. This is only called due to global errors ...
	 */
	private void wakeAllMonitors() {
		Utils.logger.fine("begin");
		synchronized (idMonitorMap) {
			for (Integer id : idMonitorMap.keySet()) {
				wakeWaitingProcess(id.intValue());
			}
		}
		Utils.logger.fine("end");
	}


	/**
	 * This method is called from worker-threads which processed an invocation and have data 
	 * ready that has to be returned to the "caller"
	 * 
	 * @param requestID the request id that is waiting for the result
	 * @param result the result itself
	 */
	protected void putResultToQueue(int requestID, Object result){
		Utils.logger.fine("begin");
		final Object monitor = idMonitorMap.get(requestID);
		synchronized (monitor) {
			synchronized (requestResults) {
				requestResults.put(requestID,result);
			}
			monitor.notify();
		}
		Utils.logger.fine("end");
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
	private Object createMonitor(final int requestID) {
		Utils.logger.fine("begin");
		final Object monitor = new Object();
		synchronized (idMonitorMap) {
			idMonitorMap.put(requestID, monitor);
		}
		Utils.logger.fine("end");
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
		Utils.logger.fine("begin");
		selectorChangeRequest(channel, ChangeRequest.REGISTER, SelectionKey.OP_READ);
		Utils.logger.fine("end");
	}


	/**
	 * 
	 * TODO Documentation to be done
	 * @param channel
	 */
	public void changeOpForReadiness(SocketChannel channel) {
		Utils.logger.fine("begin");
		selectorChangeRequest(channel, ChangeRequest.CHANGEOPS, SelectionKey.OP_READ);
		Utils.logger.fine("end");
	}
	
	/**
	 * 
	 * TODO Documentation to be done
	 * @param requestID
	 * @return
	 */
	protected Object getResult(int requestID){
		synchronized (requestResults) {
			return requestResults.remove(requestID);			
		}
	}
	
}
