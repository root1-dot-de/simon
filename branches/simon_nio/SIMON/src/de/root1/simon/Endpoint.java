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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.root1.simon.nio.ChangeRequest;
import de.root1.simon.nio.EchoWorker;

/**
 * An endpoint represents one end of the socket-connection between client and server.
 * 
 * @author ACHR
 */
public class Endpoint extends Thread {

//	/** the <code>ObjectInputStream</code> associated with the opened socket connection */
//	private ObjectInputStream objectInputStream;
//	
//	/** the <code>ObjectOutputStream</code> associated with the opened socket connection */
//	private ObjectOutputStream objectOutputStream;
	
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
	private List pendingChanges = new LinkedList();
	
	// Maps a SocketChannel to a list of ByteBuffer instances
	private Map pendingData = new HashMap();
	
	// The buffer into which we'll read data when it's available
	private ByteBuffer readBuffer = ByteBuffer.allocate(8192);
	
	// The channel on which we'll accept connections
	private ServerSocketChannel serverChannel;
	private int port = 0;
	

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
	public Endpoint(int objectCacheLifetime, LookupTable lookupTable, String threadName, boolean isServer, int port) throws IOException {
		if (Statics.DEBUG_MODE) System.out.println("Endpoint.Endpoint() -> start");
		if (objectCacheLifetime<1) throw new IllegalArgumentException("objectCacheLifetime must be >=1");
		
		this.setName("Endpoint: "+threadName);
		this.lookupTable = lookupTable;
		this.port = port;
		
		this.objectCacheLifetime = objectCacheLifetime;
		if (isServer) this.selector = this.initSelector();
		start();
		
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
	
	/*
	 * Creating the selector and server channel
	 * The astute will have noticed the call to initSelector() in the constructor. 
	 * Needless to say this method doesn't exist yet. So let's write it. It's job 
	 * is to create and initialize a non-blocking server channel and a selector. 
	 * It must and then register the server channel with that selector. 
	 */
	private Selector initSelector() throws IOException {
		// Create a new selector
		Selector socketSelector = SelectorProvider.provider().openSelector();

		// Create a new non-blocking server socket channel
		this.serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);

		// Bind the server socket to the specified address and port
		InetSocketAddress isa = new InetSocketAddress("0.0.0.0", port);
		serverChannel.socket().bind(isa);

		// Register the server socket channel, indicating an interest in 
		// accepting new connections
		serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);

		return socketSelector;
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
		System.out.println("Endpoint.accept(): Start");
		// For an accept to be pending the channel must be a server socket channel.
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

		// Accept the connection and make it non-blocking
		SocketChannel socketChannel = serverSocketChannel.accept();
		Socket socket = socketChannel.socket();
		System.out.println("Endpoint.accept(): Client: "+socket.getInetAddress());
		socketChannel.configureBlocking(false);

		socketChannel.register(selector, SelectionKey.OP_READ);
		System.out.println("Endpoint.accept(): End");
	}
	
	/**
	 * The main receive-loop
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		super.run();
		
		while (true) {
			try {
				// Process any pending changes
				// this means read/write interests on channels
				synchronized (this.pendingChanges) {
					Iterator changes = this.pendingChanges.iterator();
					while (changes.hasNext()) {
						ChangeRequest change = (ChangeRequest) changes.next();
						switch (change.type) {
						case ChangeRequest.CHANGEOPS:
							SelectionKey key = change.socket.keyFor(this.selector);
							key.interestOps(change.ops);
						}
					}
					this.pendingChanges.clear();
				}

				// Wait for an event one of the registered channels
				this.selector.select();

				// Iterate over the set of keys for which events are available
				Iterator selectedKeys = this.selector.selectedKeys().iterator();
				while (selectedKeys.hasNext()) {
					SelectionKey key = (SelectionKey) selectedKeys.next();
					selectedKeys.remove();

					if (!key.isValid()) {
						continue; // .. with next loop in "while"
					}

					// Check what event is available and deal with it
					if (key.isAcceptable()){
						accept(key);
					} else if (key.isReadable()) {
						this.read(key);
					} else if (key.isWritable()) {
						this.write(key);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void write(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		synchronized (this.pendingData) {
			List queue = (List) this.pendingData.get(socketChannel);

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
	}
	


	private void read(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		// Clear out our read buffer so it's ready for new data
		this.readBuffer.clear();
		
		// ------------------------		
		int requestID = -1;
		int msgType = -1;
		String remoteObjectName = null;
		ByteBuffer bb = ByteBuffer.allocate(5); // one byte + 4 byte integer
		socketChannel.read(bb);
		
		// Header: Get type and requestid
		msgType = bb.get();	
		requestID = bb.getInt();
		
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
		
		// ------------------------
		

		// Attempt to read off the channel
		int numRead = -1;
		try {
			numRead = socketChannel.read(this.readBuffer);
			
			// see: http://blog.strainu.ro/programming/java/using-serialization-with-non-blocking-sockets/
//			InputStream newInputStream = Channels.newInputStream(socketChannel);
			
			//we open the channel and connect
			numRead = socketChannel.read(readBuffer);
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(readBuffer.array());
			ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
			objectInputStream.readObject();
			
		} catch (IOException e) {
			// The remote forcibly closed the connection, cancel
			// the selection key and close the channel.
			key.cancel();
			socketChannel.close();
			return;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (numRead == -1) {
			// Remote entity shut the socket down cleanly. Do the
			// same from our end and cancel the channel.
			key.channel().close();
			key.cancel();
			return;
		}

		// Hand the data off to our worker thread
		Simon.worker.processData(this, socketChannel, this.readBuffer.array(), numRead);
	}
	
	public void send(SocketChannel socket, byte[] data) {
		synchronized (this.pendingChanges) {
			// Indicate we want the interest ops set changed
			this.pendingChanges.add(new ChangeRequest(socket, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));

			// And queue the data we want written
			synchronized (this.pendingData) {
				List queue = (List) this.pendingData.get(socket);
				if (queue == null) {
					queue = new ArrayList();
					this.pendingData.put(socket, queue);
				}
				queue.add(ByteBuffer.wrap(data));
			}
		}

		// Finally, wake up our selecting thread so it can make the required changes
		this.selector.wakeup();
	}


	
	public void readPacket() {


		try {

			while (!interrupted()) {


		
		
		
			} // end while

		} catch (IOException e){
			globalEndpointException = new SimonRemoteException("IOException while running receive-loop on endpoint '"+this.getName()+"': "+e.getMessage());
			wakeAllMonitors();
		} catch (ClassNotFoundException e) {
			globalEndpointException = new SimonRemoteException("ClassNotFoundException while running receive-loop on endpoint '"+this.getName()+"': "+e.getMessage());
			wakeAllMonitors();
		}
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
	
	
	




	

}
