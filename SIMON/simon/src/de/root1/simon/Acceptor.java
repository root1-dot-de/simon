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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.root1.simon.utils.Utils;

/**
 * This class waits for incoming connections on the SIMON server and 
 * forwards them to the dispatcher
 * 
 * @author Alexander Christian
 */
public class Acceptor implements Runnable {

	private int listenPort = 2000;
	private boolean isRunning = true;
	private ServerSocketChannel serverChannel;
	private Selector socketSelector;
	private Dispatcher dispatcher;
	
	protected Logger _log = Logger.getLogger(this.getClass().getName());
	private SelectionKey register;
	private InetAddress address;
	private boolean shutdown;

	/**
	 * 
	 * Creates a new Acceptor.
	 * For moving accepted channels to the dispatcher, we need a reference to an dispatcher.
	 * Also we need a Port where the Acceptor listens for new connections.
	 * @param address 
	 * 
	 * @param dispatcher the dispatcher which gets the accepted channels
	 * @param listenPort the port the server listens for incoming connections
	 * @throws IOException if there is an I/O error
	 */
	Acceptor(InetAddress address, Dispatcher dispatcher, int listenPort) throws IOException {
		_log.fine("begin");
		
		this.address = address;
		this.listenPort = listenPort;
		this.dispatcher = dispatcher;
		
		initSelector();
		_log.fine("end");
	}

	public void run() {
		_log.fine("begin");
		isRunning = true;
		while (!shutdown) {
			try {

				_log.finer("waiting for selection");
				// Wait for an event one of the registered channels
				this.socketSelector.select();

				// Iterate over the set of keys for which events are available
				Iterator<SelectionKey> selectedKeys = this.socketSelector.selectedKeys().iterator();
				
				while (selectedKeys.hasNext()) {
					SelectionKey key = (SelectionKey) selectedKeys.next();
					selectedKeys.remove();
					
					if (_log.isLoggable(Level.FINER))
						_log.finer("selected key="+Utils.getKeyIdentifierExtended(key));

					if (!key.isValid()) {
						continue;
					}

					// Check what event is available and deal with it
					if (key.isAcceptable()) {
						this.accept(key);
					}
				}
				
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		isRunning = false;
		_log.fine("end");
	}
	
	private void accept(SelectionKey key) throws IOException {
		_log.fine("begin");
		
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel(); // get the key's channeö
		
		SocketChannel clientChannel = serverSocketChannel.accept(); // get the connected client channel
		
		clientChannel.configureBlocking(false);
		
		dispatcher.registerChannel(clientChannel); // register channel on dispatcher
		
		_log.fine("end");	
	}

	
	/**
	 * Interrupts the acceptor-thread for a server shutdown
	 */
	public void shutdown(){
		_log.fine("begin");
		shutdown = true;
		if (serverChannel!=null){
			try {
				socketSelector.wakeup();
				register.cancel();
				serverChannel.close();
			} catch (IOException e) {	
				// nothing to do
			}
			
			while (isRunning) {
				_log.finest("waiting for Acceptor to shutdown...");
				try {
					Thread.sleep(Statics.WAIT_FOR_SHUTDOWN_SLEEPTIME);
				} catch (InterruptedException e) {
					// do nothing
				}
			}
			
		}
		_log.fine("end");
	}
	
	private void initSelector() throws IOException {
		_log.fine("begin");
		socketSelector = SelectorProvider.provider().openSelector();
		// Create a new non-blocking server socket channel
		this.serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);

		// Bind the server socket to the specified address and port
		InetSocketAddress isa = new InetSocketAddress(address, listenPort);
		serverChannel.socket().bind(isa);

		register = serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);
		_log.fine("end");
	} 
	
	/**
	 * Returns whether the acceptor is still in run() or not
	 * @return boolean
	 */
	protected boolean isRunning(){
		return isRunning;
	}
}
