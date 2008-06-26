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
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.logging.Logger;

import de.root1.simon.exceptions.EstablishConnectionFailed;

/**
 * This class is used by SIMON to act as a SIMON-Client
 *
 * @author achristian
 *
 */
public class Client {
	
	/** TODO describe member */
	private Dispatcher dispatcher;
	
	/** TODO describe member */
	private SocketChannel clientSocketChannel;
	
	/** TODO describe member */
	private Selector selector;
	
	/** TODO describe member */
	private SelectionKey key;
	
	protected transient Logger _log = Logger.getLogger(this.getClass().getName());
	
	/**
	 * TODO Documentation to be done
	 * @throws IOException 
	 */
	public Client(Dispatcher dispatcher) throws IOException {
		_log.fine("begin");
		this.dispatcher = dispatcher;
		_log.fine("end");
	}
	
	/**
	 * 
	 * TODO: Documentation to be done for method 'connect', by 'ACHR'..
	 * 
	 * @param host
	 * @param port
	 * @throws IOException
	 * @throws EstablishConnectionFailed
	 */
	public void connect(String host, int port) throws IOException, EstablishConnectionFailed {
		
		_log.fine("begin");
		
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
		
		_log.fine("end");
	}
	
	/**
	 * 
	 * TODO: Documentation to be done for method 'getKey', by 'ACHR'..
	 * 
	 * @return
	 */
	public SelectionKey getKey() {
		return key;
	}
	
	/**
	 * 
	 * TODO: Documentation to be done for method 'getChannelToServer', by 'ACHR'..
	 * 
	 * @return
	 */
	public SelectableChannel getChannelToServer() {
		return key.channel();
	}

}