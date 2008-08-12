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
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.logging.Logger;

import de.root1.simon.exceptions.EstablishConnectionFailed;

/**
 * This class is used by SIMON to make a socket connection to the server.
 * After connecting, one can use {@link Client#getKey()} to
 * retrieve the connection-stuff.
 *
 * @author achristian
 *
 */
public class Client {
	
	/** the dispatcher which acts on the selector */
	private Dispatcher dispatcher;
	
	/** the socketchannel the client uses to read and write his data */
	private SocketChannel clientSocketChannel;
	
	/** the selector which is used by the client */
	private Selector selector;
	
	/** the relation between the socketchannel and the selector to which the client is connected */
	private SelectionKey key;
	
	protected transient Logger _log = Logger.getLogger(this.getClass().getName());
	
	/**
	 * Creates a new instance of {@link Client}.
	 * 
	 * @param dispatcher a reference to the dispatcher where the connection is automatically 
	 * registered after the connection is established.
	 *  
	 */
	public Client(Dispatcher dispatcher) {
		_log.fine("begin");
		this.dispatcher = dispatcher;
		_log.fine("end");
	}
	
	/**
	 * 
	 * Opens a connection the the given server
	 * 
	 * @param host the servers host
	 * @param port the servers port
	 * @throws EstablishConnectionFailed if there's a problem establishing a connection to the server
	 */
	public void connect(String host, int port) throws EstablishConnectionFailed {
		
		_log.fine("begin");
		try {
			selector = SelectorProvider.provider().openSelector();
			clientSocketChannel = SocketChannel.open();
			clientSocketChannel.configureBlocking(false);
		
			// Kick off connection establishment
			clientSocketChannel.connect(new InetSocketAddress(host, port));
		
			// Queue a channel registration since the caller is not the 
			// selecting thread. As part of the registration we'll register
			// an interest in connection events. These are raised when a channel
			// is ready to complete connection establishment.
			
			clientSocketChannel.register(selector, SelectionKey.OP_CONNECT);
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
						
						_log.finer("finishing connection");
						socketChannel.finishConnect();
						_log.fine("register on dispatcher");
						dispatcher.registerChannel(socketChannel);
						
					} catch (IOException e) {
						
						// Cancel the channel's registration with our selector
						key.cancel();
						throw new EstablishConnectionFailed("could not establish connection to server. is server running? error-msg:"+e);
						
					}
					
				} else throw new IllegalStateException("invalid op event: op="+key.interestOps());
				
			}
		} catch (IOException e) {
			throw new EstablishConnectionFailed(e.getMessage());
		}
		
		_log.fine("end");
	}
	
	/**
	 * 
	 * Returns the key that holds the relation between the client's socketchannel and selector
	 * 
	 * @return SelectionKey
	 */
	public SelectionKey getKey() {
		return key;
	}
	
// UNUSED:	
//	/**
//	 * 
//	 * TODO: Documentation to be done for method 'getChannelToServer', by 'ACHR'..
//	 * 
//	 * @return
//	 */
//	public SelectableChannel getChannelToServer() {
//		return key.channel();
//	}

}