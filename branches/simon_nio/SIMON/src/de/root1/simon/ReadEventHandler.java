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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.root1.simon.exceptions.LookupFailedException;
import de.root1.simon.exceptions.SimonRemoteException;
import de.root1.simon.utils.SimonClassLoader;
import de.root1.simon.utils.Utils;

/**
 * 
 * A inner class that reads and processes the packets
 * 
 * @author ACHR
 */
class ReadEventHandler implements Runnable {
	
	protected transient Logger _log = Logger.getLogger(this.getClass().getName());

	private ByteBuffer packetBody; // the packet itself
	private String remoteObjectName;
	private final Dispatcher dispatcher;
	private byte msgType;
	private int requestID;
	private SelectionKey key;


	public ReadEventHandler(SelectionKey key, Dispatcher dispatcher) {
		_log.fine("begin");
		this.key = key;
		this.dispatcher = dispatcher;
		_log.fine("end");
	}

	public void run() {
		_log.fine("begin");

		try {
			
			SocketChannel socketChannel =(SocketChannel) key.channel();
			
			RxPacket rxPacket = new RxPacket(socketChannel);
			key.attach(null);
			dispatcher.changeOpForReadiness(socketChannel);
			
	        if (_log.isLoggable(Level.FINEST)) {
	        	_log.finest(Utils.inspectPacket(rxPacket.getByteBuffer()));
	        }
			
			_log.finer("interpreting packet ...");
			msgType = rxPacket.getMsgType();
			requestID = rxPacket.getRequestID();

			if (_log.isLoggable(Level.FINER))
				_log.finer("got requestID="+requestID);

			packetBody = rxPacket.getBody();
			
//			if (_log.isLoggable(Level.FINER)){
//				byte[] b = packetBody.array();
//				for (int i = 0; i < b.length; i++) {
//					byte c = b[i];
//					
//					_log.finer("body b["+i+"]="+c);
//
//				}
//			}
			
			
			// if the received data is a new request ...
			switch (msgType) {
				
				case Statics.INVOCATION_PACKET:
					processInvokeMethod(remoteObjectName);
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
					
				case Statics.PING_PACKET :
					processPing();
					break;
					
				case Statics.INVOCATION_RETURN_PACKET :
					Object result = Utils.unwrapValue(dispatcher.removeRequestReturnType(requestID), packetBody);
					dispatcher.putResultToQueue(requestID, result);
					break;
					
				case Statics.LOOKUP_RETURN_PACKET :
					dispatcher.putResultToQueue(requestID, Utils.getObject(packetBody));
					break;
					
				case Statics.TOSTRING_RETURN_PACKET :
					dispatcher.putResultToQueue(requestID, Utils.getString(packetBody));
					break;
				
				case Statics.HASHCODE_RETURN_PACKET :
					dispatcher.putResultToQueue(requestID, packetBody.getInt());
					break;
					
				case Statics.EQUALS_RETURN_PACKET :
					dispatcher.putResultToQueue(requestID, (Boolean.valueOf(packetBody.get()==1 ? true : false)));
//					dispatcher.wakeWaitingProcess(requestID);
					break;
					
				case Statics.PONG_PACKET :
					processPong();
					break;
					
					
				default :
//					interrupt();
//					globalEventHandlerException = new SimonRemoteException("invalid packet received from EventHandler ...");
//					try {
//						socketChannel.close();
//						key.cancel();
//					} catch (IOException e) {
//					}
					// FIXME do something!
					throw new SimonRemoteException("invalid packet received from EventHandler ...");
//					break;
			}

			
		} catch (IOException e) {
			key.cancel();
			_log.severe("io exception: "+e.getMessage());
			// FIXME for testing only
			System.exit(1);
			// TODO check where we can put the exception as a result back to the requester, or how we have to handle the exception
//			dispatcher.putResultToQueue(requestID, new SimonRemoteException(e.getMessage()));
		} catch (ClassNotFoundException e) {
//			dispatcher.putResultToQueue(requestID, new SimonRemoteException(e.getMessage()));
			key.cancel();
			_log.warning("class not found: "+e.getMessage());
			// FIXME for testing only
			System.exit(1);
			
		} catch (LookupFailedException e) {
			_log.fine("lookup failed!");
			dispatcher.putResultToQueue(requestID, e);
		}
		_log.fine("end");
	}

	private void processPong() {
		if (_log.isLoggable(Level.FINE))
			_log.fine("begin. requestID="+requestID);
		
		_log.finer("PONG PACKET RECEIVED");
		dispatcher.putResultToQueue(requestID, packetBody.get());
		
		if (_log.isLoggable(Level.FINE))
			_log.fine("end. requestID="+requestID);
	}

	private void processPing() {
		if (_log.isLoggable(Level.FINE))
			_log.fine("begin. requestID="+requestID);
		
		_log.finer("PING PACKET RECEIVED ... SENDING PONG PACKET");
		TxPacket p = new TxPacket();
		p.setHeader(Statics.PONG_PACKET, requestID);
		p.put((byte)0x00);
		p.setComplete();
		dispatcher.send(key,p.getByteBuffer());
		
		if (_log.isLoggable(Level.FINE))
			_log.fine("end. requestID="+requestID);
	}

	/**
	 * 
	 * processes a request for a "hashCode()" call on a remote-object
	 * 
	 * @param requestID
	 * @param remoteObjectName
	 * @throws IOException
	 * @throws LookupFailedException 
	 */
	private void processHashCode(String remoteObjectName) throws IOException, LookupFailedException {
		_log.fine("begin");
		final int hashcode = dispatcher.getLookupTable().getRemoteBinding(remoteObjectName).hashCode();		
		
		ByteBuffer packet = ByteBuffer.allocate(1+4+4);
		packet.put(Statics.HASHCODE_RETURN_PACKET);
		packet.putInt(requestID);
		packet.putInt(hashcode);
		
		dispatcher.send(key,packet);
		_log.fine("end");
	}
	
	/**
	 * 
	 *processes a request for a "toString()" call on a remote-object
	 * 
	 * @param requestID
	 * @param remoteObjectName
	 * @throws IOException
	 * @throws LookupFailedException 
	 */
	private void processToString(String remoteObjectName) throws IOException, LookupFailedException {
		_log.fine("begin");
		final String tostring = dispatcher.getLookupTable().getRemoteBinding(remoteObjectName).toString();		
		
		ByteBuffer packet = ByteBuffer.allocate(1+4+(4+tostring.length()));
		packet.put(Statics.TOSTRING_RETURN_PACKET);
		packet.putInt(requestID);
		packet.put(Utils.stringToBytes(tostring));
		
		dispatcher.send(key,packet);
		_log.fine("end");
	}
	
	/**
	 * 
	 * processes a request for a "equls()" call on a remote-object
	 * 
	 * @param requestID
	 * @param remoteObjectName
	 * @praram object
	 * @throws IOException
	 * @throws LookupFailedException 
	 */
	private void processEquals(String remoteObjectName, Object object) throws IOException, LookupFailedException{
		_log.fine("begin");
		final boolean equals = dispatcher.getLookupTable().getRemoteBinding(remoteObjectName).equals(object);		

		ByteBuffer packet = ByteBuffer.allocate(1+4+1);
		packet.put(Statics.EQUALS_RETURN_PACKET);
		packet.putInt(requestID);
		packet.put(equals ? (byte) 1 : (byte) 0);
		
		dispatcher.send(key,packet);
		_log.fine("end");
	}
	
	/**
	 * 
	 * processes a lookup
	 * @throws IOException 
	 * @throws LookupFailedException 
	 */
	private void processLookup(String remoteObjectName) throws IOException {
		_log.fine("begin");
		byte[] remoteObjectInterface;
		try {
			remoteObjectInterface = Utils.objectToBytes(dispatcher.getLookupTable().getRemoteBinding(remoteObjectName).getClass().getInterfaces());		
		} catch (LookupFailedException e) {
			remoteObjectInterface = Utils.objectToBytes(e);
		}
		
		TxPacket p = new TxPacket();
		p.setHeader(Statics.LOOKUP_RETURN_PACKET, requestID);
		p.put(remoteObjectInterface);
		p.setComplete();
		dispatcher.send(key,p.getByteBuffer());
		
		_log.fine("end");
	}
	
	/**
	 * 
	 * TODO Documentation to be done
	 * @param remoteObjectName
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws LookupFailedException 
	 */
	private void processInvokeMethod(String remoteObjectName) throws IOException, ClassNotFoundException, LookupFailedException{
		
		if (_log.isLoggable(Level.FINE))
			_log.fine("begin. requestID="+requestID);
		
		remoteObjectName = Utils.getString(packetBody);
		final long methodHash = packetBody.getLong();
		
		final Method method = dispatcher.getLookupTable().getMethod(remoteObjectName, methodHash);
		final Class<?>[] parameterTypes = method.getParameterTypes();			
		final Object[] args = new Object[parameterTypes.length];
		
		if (_log.isLoggable(Level.FINER)){
			_log.finer("calling method name="+method.getName());
		}
		
		// unwrapping the arguments
		for (int i = 0; i < args.length; i++) {
			args[i]=Utils.unwrapValue(parameterTypes[i], packetBody);							
		}
		
		Object result = null;
		
		try {			
			// replace existing SimonRemote objects with proxy object
			if (args != null) {
	
				for (int i = 0; i < args.length; i++) {
					
					// search the arguments for remote-objects for callbacks
					if (args[i] instanceof SimonCallback) {
						
						final SimonCallback simonCallback = (SimonCallback) args[i];
						_log.finer("SimonCallback in args found. id="+simonCallback.getId());					
											
						Class<?>[] listenerInterfaces = new Class<?>[1];
						listenerInterfaces[0] = Class.forName(simonCallback.getInterfaceName());

						// reimplant the proxy object
						args[i] = Proxy.newProxyInstance(SimonClassLoader.getClassLoader(this.getClass()), listenerInterfaces, new SimonProxy(dispatcher, key, simonCallback.getId()));
						_log.finer("proxy object for SimonCallback injected");
					} 
				} 
			} 
			
			try {

				if (_log.isLoggable(Level.FINER))
					_log.finer("start invoking method='"+method+"'. requestID="+requestID);
				
				result = method.invoke(dispatcher.getLookupTable().getRemoteBinding(remoteObjectName), args);

				if (_log.isLoggable(Level.FINER))
					_log.finer("end invoking method='"+method+"'. requestID="+requestID+" result="+result);

				
				// Search for SimonRemote in result
				if (result instanceof SimonRemote){
					_log.finer("Result of method is instance of SimonRemote");
					
					SimonCallback simonCallback = new SimonCallback(key,(SimonRemote)result);
					simonCallback.getId();

					dispatcher.getLookupTable().putRemoteBinding(simonCallback.getId(), (SimonRemote)result);
					result = simonCallback;;
					
				}
				
			} catch (InvocationTargetException e){
				result = e.getTargetException();
			} 
			_log.finer("sending answer");
			TxPacket packet = new TxPacket();
			packet.setHeader(Statics.INVOCATION_RETURN_PACKET, requestID);
			
			packet = Utils.wrapValue(method.getReturnType(), result, packet); // wrap the result
			packet.setComplete();
			dispatcher.send(key, packet.getByteBuffer());
				
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
		//Utils.debug("ReadEventHandler.processInvokeMethod() -> end. requestID="+requestID);
		if (_log.isLoggable(Level.FINE))
			_log.fine("end. requestID="+requestID);
	}
	
	
	
}