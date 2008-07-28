/*
 * Copyright (C) 2008 Alexander Christian <alex(at)root1.de>. All rights reserved.
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
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
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
import java.util.logging.Logger;

import de.root1.simon.exceptions.ConnectionException;
import de.root1.simon.exceptions.LookupFailedException;
import de.root1.simon.exceptions.SimonRemoteException;
import de.root1.simon.utils.Utils;

/**
 * This is the "main" class of Simon. Each packet has to be handled by this class.<br>
 * There's only one dispatcher per client/server.
 * 
 * @author ACHR
 */
public class Dispatcher implements Runnable {
	
	protected transient Logger _log = Logger.getLogger(this.getClass().getName());

	/** The table that holds all the registered/bind remote objects */
	private LookupTable lookupTable;
	
	/** a simple counter that is used for creating request IDs */
	private int requestIdCounter = 0;
	
	/** The map that holds the relation between the request ID and the corresponding monitor */
	private HashMap<Integer, Object> idMonitorMap = new HashMap<Integer, Object>();

	/** The map that holds the relation between a current active request and a connected SelectableChannel */
	private HashMap<Integer, SelectableChannel> idSelectableChannelMap = new HashMap<Integer, SelectableChannel>();
	
	/** The map that holds the relation between the request ID and the received result */
	private HashMap<Integer, Object> requestResults = new HashMap<Integer, Object>();
	
	/** a memory map for the client the unwrap the incoming return value after executing a method on the server */
	private HashMap<Integer, Class<?>> requestReturnType = new HashMap<Integer, Class<?>>();
	
	// -----------------------
	// NIO STUFF
	
	/** The selector we'll be monitoring. This contains alle the connected channels/clients */
	private Selector selector;
	
	/** A list of PendingChange instances.  */
	private List<ChangeRequest> pendingSelectorChanges = new LinkedList<ChangeRequest>();
	
	/** Maps a SocketChannel to a list of ByteBuffer instances */
	private Map<SocketChannel, List<ByteBuffer>> pendingData = new HashMap<SocketChannel, List<ByteBuffer>>();
	
	/** the thread-pool where the worker-threads live in */
	private ExecutorService eventHandlerPool = null;

	/** Shutdownflag. If set to true, the dispatcher is going to shutdown itself and all related stuff */
	private boolean shutdown;

	/** indicates if the dispatcher is running or not */
	private boolean isRunning;

	/** a instance of the distributed garbage collector */
	private DGC dgc;

	/** an identifier string to determine to which server this dispatcher is connected to  */
	private String serverString;
	
	private Object incomingInvocationCounterMonitor = new Object();
	private int incomingInvocationCounter = 0;

	private Object outgoingInvocationCounterMonitor = new Object();
	private int outgoingInvocationCounter;

	/**
	 * 
	 * Creates a packet dispatcher which delegates 
	 * the packet-reading to {@link ReadEventHandler} threads in the given <code>threadPool</code>
	 * 
	 * @param serverString an identifier string to determine to which server this dispatcher is 
	 * connected to. this must be set to <code>null</code> if this dispatcher is a server dispatcher.
	 * @param lookupTable the global lookup table
	 * @param threadPool the pool the worker threads live in
	 * @throws IOException if the {@link Selector} cannot be initiated
	 */
	public Dispatcher(String serverString, LookupTable lookupTable, ExecutorService threadPool) throws IOException {
		_log.fine("begin");
		
		this.serverString = serverString;
		this.lookupTable = lookupTable;
		this.eventHandlerPool = threadPool;
		this.selector = SelectorProvider.provider().openSelector();
		this.dgc = new DGC(this);
		dgc.start();
		_log.fine("end");
	}
	
	/**
	 * 
	 * Generates a request ID<br>
	 * IDs have a unique value from 0..Integer.MAX_VALUE<br>
	 * The range should be big enough so that there should 
	 * not be two oder more identical IDs
	 * 
	 * @return a request ID
	 */
	private synchronized Integer generateRequestID() {
		return (++requestIdCounter == Integer.MAX_VALUE ? 0 : requestIdCounter);
	}
	
	/**
	 * The main receive-loop
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		
		_log.fine("begin");
		
		isRunning = true;
		SelectionKey key = null;
		
		while (!shutdown) {
			
			try {
				
				processPendingSelectorChanges();

				_log.finer("W A I T I N G for an event");
				int numOfselectableKeys = selector.select();

				if (numOfselectableKeys>0) {

					if (_log.isLoggable(Level.FINER)) {
						_log.finer(numOfselectableKeys+ " keys ready");
					}
					
					// Iterate over the set of keys for which events are available
					Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
					while (selectedKeys.hasNext()) {
					
						key = (SelectionKey) selectedKeys.next();
						selectedKeys.remove();
						 
						if (_log.isLoggable(Level.FINER)){

							_log.finer("processing key="+Utils.getKeyString(key));
							
						}
						
						if (!key.isValid()) {
							_log.finer("key is invalid: "+key);
							continue; // .. with next in "while"
						}
	
						// Check what event is available and deal with it
						if (key.isAcceptable()){ // used by the server

							_log.finer("key is acceptable. Accepting is done by the 'Acceptor'!");
							
						} else if (key.isConnectable()) { // used by the client

							_log.finer("key is connectable.  FinishConnection is done by the 'Client'!");
							
						} else if (key.isReadable()) {

							_log.finer("key is readable");

							key.interestOps(key.interestOps() & ~SelectionKey.OP_READ); // deregister for read-events
							key.attach(true); // remember that this key is currently being read ...
							
							handleRead(key);
							
						} else if (key.isWritable()) {
							
							if (_log.isLoggable(Level.FINER))
								_log.finer("key  is writeable");
							
							key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE); // deregister for write events

							handleWrite(key);
							
						}
					}

				} else {
				
						_log.finer("no keys available");
				}
				
			} catch (IOException e) {
				
				_log.warning("I/O Exception: "+e.getMessage());
				
				if (key!=null) {
					cancelKey(key);
					lookupTable.unreference(key);
				}
				
			} catch (Exception e) {
				// TODO correct exception handling here?
				_log.severe("Generel Exception: e="+e+" msg="+e.getMessage());e.printStackTrace();		

				wakeAllMonitors();
				cancelKey(key);
				lookupTable.unreference(key);
			}
		}
		isRunning = false;
	}


	/**
	 * Process any pending selector changes. 
 	 * (changing read/write interests and registrations)
	 */
	private void processPendingSelectorChanges() throws ClosedChannelException {
		
		synchronized (this.pendingSelectorChanges) {
			
			Iterator<ChangeRequest> changes = this.pendingSelectorChanges.iterator();
			while (changes.hasNext()) {
				
				ChangeRequest change = (ChangeRequest) changes.next();
				
				if (_log.isLoggable(Level.FINER))
					_log.finer("changerequest: "+change);
				
				switch (change.type) {

					case ChangeRequest.CHANGEOPS:
						
						SelectionKey key = change.socket.keyFor(this.selector);
						
						if (key==null) {
							if (_log.isLoggable(Level.FINER)) {
								_log.finer("changing ops not possible. key for socketchannel "+Utils.getChannelString(change.socket)+" is 'gone'... changerequest was: "+change);
							}
							dgc.removeKey(key);
							cancelWaitingMonitors(change.socket);
							
						} else if (key.attachment()!=null && change.ops==SelectionKey.OP_READ){
							if (_log.isLoggable(Level.FINER)) {
								_log.finer("cannot change to read mode, key="+Utils.getKeyString(key)+" is currently busy with reading. cannot interrupt him!");
							}
//							System.err.flush();
//							System.exit(1);
						}
						else {
							key.interestOps(change.ops);
						}
						break;
						
					case ChangeRequest.REGISTER:

						SelectionKey connectedClientKey = change.socket.register(this.selector, change.ops);
						dgc.addKey(connectedClientKey);
						break;
						
				}
			}
			this.pendingSelectorChanges.clear();
		}
		// -------------
	}



	private void handleRead(SelectionKey key) {
		_log.fine("begin");
		eventHandlerPool.execute(new ReadEventHandler(key,this));
		_log.fine("end");
	}
	
	private void handleWrite(SelectionKey key) throws IOException {
		_log.fine("begin");
		SocketChannel socketChannel = (SocketChannel) key.channel();
		
		if (_log.isLoggable(Level.FINER)) {
			_log.finer("sending data for key=" + Utils.getKeyString(key) + " from queue");
		}
		
		synchronized (pendingData) {
		
			List<ByteBuffer> queue = (List<ByteBuffer>) pendingData.get(socketChannel);

			// Write until there's not more data ...
			while (!queue.isEmpty()) {
				
				ByteBuffer buf = (ByteBuffer) queue.get(0);

				if (_log.isLoggable(Level.FINEST)) {
					_log.finest(Utils.inspectPacket(buf));
				}

				socketChannel.write(buf);
				
				
				if (buf.remaining() > 0) {
					System.err.println("Dispatcher#handleWrite() -> not enough bytes written!");
					System.err.flush();
					System.exit(0);
				}
				queue.remove(0);
				
			}

			// We wrote away all data, so we're no longer interested
			// in writing on this socket. Switch back to waiting for
			// data.
			if (queue.isEmpty()) {
				
				
				if (_log.isLoggable(Level.FINE)) {
					_log.fine("sending is done. no more data in queue. return key="+Utils.getKeyString(key)+" to OP_READ");
				}
				
				// ... but first check if key is "busy with reading" ...
				Boolean keyInReadProcess = (Boolean) key.attachment();
				
				if (keyInReadProcess!=null && keyInReadProcess.booleanValue()){
					
					_log.finer("key is currently being read!!! do nothing!!!");
					
				} else {
				
					_log.finer("key is not in use, set OP to OP_READ!");
					selectorChangeRequest(socketChannel, ChangeRequest.CHANGEOPS, SelectionKey.OP_READ);
					
				}
			}
		}
		_log.fine("end");
	}
	
	/**
	 * Sends the data to the socket channel Be warned: only the data from start
	 * to position is sent
	 */
	protected void send(SelectionKey key, ByteBuffer packet) throws CancelledKeyException {
		_log.fine("begin");
		
		SocketChannel socketChannel = (SocketChannel) key.channel();

		if (_log.isLoggable(Level.FINER)){
			_log.finer("sending data for key="+Utils.getKeyString(key));
		}
		
		
		// queue the data we want written
		synchronized (this.pendingData) {
			List<ByteBuffer> queue = this.pendingData.get(socketChannel);
			if (queue == null) {
				queue = new ArrayList<ByteBuffer>();
				this.pendingData.put(socketChannel, queue);
			}
				
			queue.add(packet);
			
		}
		if (_log.isLoggable(Level.FINEST)){
			_log.finer("added packet for key="+Utils.getKeyString(key)+" with limit="+packet.limit()+" position="+packet.position()+" to queue");
		}
		if (_log.isLoggable(Level.FINER)) {
			_log.finer("changing key="+Utils.getKeyString(key)+" to OP_WRITE");
		}
		selectorChangeRequest(socketChannel, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE);

		_log.fine("end");
	}


	/**
	 * 
	 * Changes the interested operation of a specific {@link SocketChannel}
	 *  
	 * @param socketChannel the channel which wants to change its interested operation
	 * @param type the the of the operation: {@link ChangeRequest#CHANGEOPS} or {@link ChangeRequest#REGISTER} 
	 * @param operation the new operation. One of SelectionKey#OP_* ...
	 */
	private void selectorChangeRequest(SocketChannel socketChannel, int type, int operation) {
		_log.fine("begin");

		if (_log.isLoggable(Level.FINER)){
			_log.finer("got changerequest for client "+Utils.getChannelString(socketChannel)+" -> type="+type+" operation="+Utils.getSelectionKeyString(operation));
		}
		
		synchronized (pendingSelectorChanges) {
			// Indicate we want the interest ops set changed
			pendingSelectorChanges.add(new ChangeRequest(socketChannel, type, operation));
		}
				
		// Finally, wake up our selecting thread so it can make the required changes
		selector.wakeup();
		
		_log.fine("end");
	}

	/**
	 * 
	 * Sends a remoteobject lookup to the server
	 * 
	 * @param remoteObjectName
	 * @return the object we made the lookup for
	 * @throws SimonRemoteException 
	 * @throws IOException 
	 */
	protected Object invokeLookup(SelectionKey key, String remoteObjectName) throws LookupFailedException, SimonRemoteException, IOException {
		
		final int requestID = generateRequestID(); 
		
		if (_log.isLoggable(Level.FINE)) {
			_log.fine("begin requestID="+requestID+" key="+Utils.getKeyString(key));
		}

 		// create a monitor that waits for the request-result
		final Object monitor = createMonitor(key.channel(), requestID);
		
		byte[] remoteObject = Utils.stringToBytes(remoteObjectName);
		
		TxPacket packet = new TxPacket();
		packet.setHeader(Statics.LOOKUP_PACKET, requestID);
		packet.put(remoteObject);
		packet.setComplete();
		
		// send the packet to the connected channel
		send(key, packet.getByteBuffer());	
		
		if (_log.isLoggable(Level.FINER))
			_log.finer("data send. waiting for answer for requestID="+requestID);
		
		// check if need to wait for the result
		synchronized (requestResults) {
			if (requestResults.containsKey(requestID))
				return getRequestResult(requestID);
		}
		
		// got to sleep until result is present
		synchronized (monitor) {
			try {
				monitor.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
			
		if (_log.isLoggable(Level.FINER))
			_log.finer("got answer for requestID="+requestID);
			
		// get result
		synchronized (requestResults) {
			if (_log.isLoggable(Level.FINE))
				_log.fine("end requestID="+requestID);
			return getRequestResult(requestID);			
		}

	}

	/**
	 * 
	 * Checks if the request result is an {@link SimonRemoteException}. 
	 * If yes, the exception is thrown, if not, the result is returned
	 * 
	 * @param requestID the request-id related to the result
	 * @return the result of the request
	 */
	private Object getRequestResult(final int requestID) {
		Object o = requestResults.remove(requestID); 
		if (o instanceof SimonRemoteException) {
			throw ((SimonRemoteException) o);
		}
		return o;
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
 		
 		if (_log.isLoggable(Level.FINE))
 			_log.fine("begin. requestID="+requestID+" key="+Utils.getKeyString(key));
 		
 		incOutgoingInvocationCounter();
 		
 		// create a monitor that waits for the request-result
		final Object monitor = createMonitor(key.channel(), requestID);
		
		// memory the return-type for later unwrap
		synchronized (requestReturnType) {
			requestReturnType.put(requestID, returnType);
		}
		
		// register callback objects in the lookup-table
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				if (args[i] instanceof SimonRemote) {
					SimonCallback sc = new SimonCallback(key,(SimonRemote)args[i]);
					if (_log.isLoggable(Level.FINER)){
						_log.fine("SimonCallback found! id="+sc.getId());
					}
					
//					lookupTable.putRemoteBinding(sc.getId(), (SimonRemote)args[i]);
					lookupTable.putRemoteCallbackBinding(key, sc.getId(), (SimonRemote) args[i]);
					
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

			// check if need to wait for the result
			synchronized (requestResults) {
				if (requestResults.containsKey(requestID))
					return getRequestResult(requestID);
			}
		
			// got to sleep until result is present
			try {
				monitor.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		if (_log.isLoggable(Level.FINE))
			_log.fine("end. requestID="+requestID);

		synchronized (requestResults) {
			return getRequestResult(requestID);
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
		final int requestID = generateRequestID();

		if (_log.isLoggable(Level.FINE))
 			_log.fine("begin. requestID="+requestID+" key="+Utils.getKeyString(key));
		
		// create a monitor that waits for the request-result
		final Object monitor = createMonitor(key.channel(), requestID);

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
			
		// get result
		synchronized (requestResults) {
			if (_log.isLoggable(Level.FINE))
	 			_log.fine("end. requestID="+requestID);
			return (String)getRequestResult(requestID);			
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
		final int requestID = generateRequestID();
		
		if (_log.isLoggable(Level.FINE))
 			_log.fine("begin. requestID="+requestID+" key="+Utils.getKeyString(key));
		
		// create a monitor that waits for the request-result
		final Object monitor = createMonitor(key.channel(), requestID);

		TxPacket packet = new TxPacket();
		packet.setHeader(Statics.HASHCODE_PACKET, requestID);
		packet.put(Utils.stringToBytes(remoteObjectName));
		packet.setComplete();
		
		send(key, packet.getByteBuffer());
		
		// check if need to wait for the result
		synchronized (requestResults) {
			if (requestResults.containsKey(requestID))
				return (Integer)getRequestResult(requestID);
		}

		// got to sleep until result is present
		try {
			monitor.wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
			
		// get result
		synchronized (requestResults) {
			if (_log.isLoggable(Level.FINE))
	 			_log.fine("end. requestID="+requestID);
			return (Integer)getRequestResult(requestID);			
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
		final int requestID = generateRequestID();
		
		if (_log.isLoggable(Level.FINE))
 			_log.fine("begin. requestID="+requestID+" key="+Utils.getKeyString(key));
		
		// create a monitor that waits for the request-result
		final Object monitor = createMonitor(key.channel(), requestID);

		TxPacket packet = new TxPacket();
		packet.setHeader(Statics.EQUALS_PACKET, requestID);
		packet.put(Utils.stringToBytes(remoteObjectName));
		packet.put(Utils.objectToBytes(object));
		packet.setComplete();
		
		send(key, packet.getByteBuffer());
		// check if need to wait for the result
		synchronized (requestResults) {
			if (requestResults.containsKey(requestID))
				return (Boolean)getRequestResult(requestID);
		}
		
		// got to sleep until result is present
		try {
			monitor.wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if (_log.isLoggable(Level.FINE))
 			_log.fine("end. requestID="+requestID);
		// get result
		return (Boolean) getRequestResult(requestID);
	}


	/**
	 * Wake the process with the related requestID
	 * 
	 * @param requestID the process to wake  
	 */
	protected void wakeWaitingProcess(int requestID) {
		
		if (_log.isLoggable(Level.FINE))
			_log.fine("begin. wakeing requestID="+requestID);
		
		synchronized (idMonitorMap) {
			synchronized (idSelectableChannelMap) {
		
				final Object monitor = idMonitorMap.remove(requestID);
				idSelectableChannelMap.remove(requestID);
				
				if (monitor!=null) {
					synchronized (monitor) {
						monitor.notify(); // wake the waiting method

						if (_log.isLoggable(Level.FINER))
				 			_log.finer("id="+requestID+" monitor="+monitor+" waked");
					}
					
				} else {
					if (_log.isLoggable(Level.FINER)) {
						_log.finer("no monitor for requestID="+requestID+" idmonitormapsize="+idMonitorMap.size());
					}
				}
				
			}
		}
		if (_log.isLoggable(Level.FINE))
 			_log.fine("end. wakeing requestID="+requestID);
	}

	/** 
	 * wake all waiting processes. This is only called due to global errors ...
	 */
	private void wakeAllMonitors() {
		_log.fine("begin");
		List<Integer> idList = new ArrayList<Integer>();
		
		synchronized (idMonitorMap) {
			for (Integer id: idMonitorMap.keySet()){
				idList.add(id);
			}
		}
			
			for (Integer id : idList) {
				wakeWaitingProcess(id.intValue());
			}
		_log.fine("end");
	}


	/**
	 * This method is called from worker-threads which processed an invocation and have data 
	 * ready that has to be returned to the "caller".
	 * after adding the result to the queue, the waiting request-method is waked.
	 * 
	 * @param requestID the request id that is waiting for the result
	 * @param result the result itself
	 */
	protected void putResultToQueue(int requestID, Object result){
		_log.fine("begin");
		
		if (_log.isLoggable(Level.FINER))
			_log.finer("requestID="+requestID+" result="+result);
		
		synchronized (requestResults) {
			requestResults.put(requestID,result);
		}
		wakeWaitingProcess(requestID);
		_log.fine("end");
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
	 * @param selectableChannel 
	 * 
	 * @param requestID
	 * @return the monitor used for waiting for the result
	 */
	private Object createMonitor(SelectableChannel selectableChannel, final int requestID) {
		_log.fine("begin");
		
		final Object monitor = new Object();
		synchronized (idMonitorMap) {
			synchronized (idSelectableChannelMap) {
				
				idMonitorMap.put(requestID, monitor);
				idSelectableChannelMap.put(requestID, selectableChannel);
				
				if (_log.isLoggable(Level.FINER)){
						_log.finer("created monitor for requestID="+requestID);
				}
				
			}
		}
		
		_log.fine("end");
		return monitor;
	}
	
	/**
	 * Returns a list of waiting request-ids related to the given SelectableChannel
	 * 
	 * @param selectableChannel
	 * @return a list of waiting request-ids related to the given SelectableChannel
	 */
	private List<Integer> getRequestId(SelectableChannel selectableChannel){
		
		List<Integer> idList = new ArrayList<Integer>();
		
		synchronized (idSelectableChannelMap) {
			
			if (idSelectableChannelMap.containsValue(selectableChannel)){
				Iterator<Integer> iterator = idSelectableChannelMap.keySet().iterator();
				while (iterator.hasNext()){
					Integer requestID = iterator.next();
					SelectableChannel selectableChannel2 = idSelectableChannelMap.get(requestID);
					if (selectableChannel==selectableChannel2) idList.add(requestID);
				}
			}
			
		}
		
		return idList;
	}
	
	private void cancelWaitingMonitors(SelectableChannel selectableChannel){
		List<Integer> requestIdList = getRequestId(selectableChannel);
		for (Integer id : requestIdList) {
			putResultToQueue(id, new ConnectionException("Connection is broken!"));
			wakeWaitingProcess(id);
		}
	}

	/**
	 * 
	 * TODO: Documentation to be done for method 'removeRequestReturnType', by 'ACHR'..
	 * 
	 * @param requestID
	 * @return
	 */
	protected Class<?> removeRequestReturnType(int requestID) {
		
		synchronized (requestReturnType) {
			return requestReturnType.remove(requestID);
		}
		
	}
	
	/**
	 * Registers a channel with the dispatcher's selector
	 * @param channel
	 * @throws ClosedChannelException 
	 */
	protected void registerChannel(SocketChannel channel) throws ClosedChannelException {
		_log.fine("begin");
		if (_log.isLoggable(Level.FINE)){
			_log.fine("registering client " + Utils.getChannelString(channel) + " for [OP_READ] on dispatcher.");
		}
		
		selectorChangeRequest(channel, ChangeRequest.REGISTER, SelectionKey.OP_READ);
		selector.wakeup();
		_log.fine("end");
	}





	/**
	 * 
	 * TODO Documentation to be done
	 * @param channel
	 */
	public void changeOpForReadiness(SocketChannel channel) {
		_log.fine("begin");
		selectorChangeRequest(channel, ChangeRequest.CHANGEOPS, SelectionKey.OP_READ);
		_log.fine("end");
	}
	
	/**
	 * 
	 * All received results are saved in a queue. With this method you can get the received result 
	 * by its requestID.
	 * <br/>
	 * <b>Attention:</b> Be sure that you only call this method if you were notified by the receiver! 
	 * 
	 * @param requestID the requestID which is related to the result
	 * @return the received result
	 */
	protected Object getResult(int requestID){
		synchronized (requestResults) {
			return getRequestResult(requestID);			
		}
	}

	/**
	 * 
	 * Sends a "ping" packet to the opposite. This has to be replied with a "pong" packet.
	 * This method is (mainly) used by the DGC to check whether the client is available or not.
	 * 
	 * @param key
	 * @return
	 */
	protected long sendPing(SelectionKey key) {
		
		if (!key.isValid()) {
			dgc.removeKey(key);
			return -1;
		}
		
		final int requestID = generateRequestID();
		
		if (_log.isLoggable(Level.FINE))
 			_log.fine("begin. requestID="+requestID+" key="+Utils.getKeyString(key));
		
		// create a monitor that waits for the request-result
		final Object monitor = createMonitor(key.channel(), requestID);

		TxPacket packet = new TxPacket();
		packet.setHeader(Statics.PING_PACKET, requestID);
		packet.put((byte)0x00);
		packet.setComplete();
		long startPing;
		synchronized (monitor) {
			startPing = System.nanoTime();
			send(key, packet.getByteBuffer());
			
			// check if need to wait for the result
			synchronized (requestResults) {
				if (requestResults.containsKey(requestID)){
					if (_log.isLoggable(Level.FINE))
			 			_log.fine("end. requestID="+requestID);
					getRequestResult(requestID);			
					long receivePong = System.nanoTime();
					return receivePong-startPing;
				}
			}
	
			// got to sleep until result is present
			try {
				monitor.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
			
		// get result
		synchronized (requestResults) {
			if (_log.isLoggable(Level.FINE))
	 			_log.fine("end. requestID="+requestID);
			getRequestResult(requestID);			
			long receivePong = System.nanoTime();
			return receivePong-startPing;
		}
	}

	/**
	 * 
	 * Initiates a shutdown of the dispatcher
	 *
	 */
	public void shutdown() {
		_log.fine("begin");
		shutdown = true;
		selector.wakeup();
		dgc.shutdown();
		eventHandlerPool.shutdown();
		while (isRunning || dgc.isRunning() || !eventHandlerPool.isShutdown()) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// nothing to do
			}
		}
		_log.fine("end");
	}


	/**
	 * 
	 * Cancels a key and all waiting monitors.
	 * 
	 * @param key the key to cancel
	 */
	public void cancelKey(SelectionKey key) {
		_log.fine("begin");
		cancelWaitingMonitors(key.channel());
		key.cancel();
		selector.wakeup();
		_log.fine("end");
	}
	
	/**
	 * 
	 * Returns the identifier string which determines to which server this dispatcher is connected to
	 * 
	 * @return the identifier string. this is <code>null</code> if this dispatcher is a server dispatcher
	 */
	public String getServerString() {
		return serverString;
	}
	
	
	protected void incIncomingInvocationCounter() {
		synchronized (incomingInvocationCounterMonitor) {
			incomingInvocationCounter++;
		}
	}
	
	protected int getIncomingInvocationCounter() {
		synchronized (incomingInvocationCounterMonitor) {
			int x = incomingInvocationCounter;
			incomingInvocationCounter = 0;
			return x;
		}
	}

	protected void incOutgoingInvocationCounter() {
		synchronized (outgoingInvocationCounterMonitor) {
			outgoingInvocationCounter++;
		}
	}
	
	protected int getOutgoingInvocationCounter() {
		synchronized (outgoingInvocationCounterMonitor) {
			int x = outgoingInvocationCounter;
			outgoingInvocationCounter = 0;
			return x;
		}
	}
	
	
	
}
