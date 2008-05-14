package de.root1.simon;

import java.io.IOException;
import java.net.InetSocketAddress;
//import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.root1.simon.utils.Utils;

public class Acceptor implements Runnable {

	private int serverPort = 2000;
	private boolean isRunning = true;
	private ServerSocketChannel serverChannel;
	private Selector socketSelector;
	private Dispatcher dispatcher;
	
	protected Logger _log = Logger.getLogger(this.getClass().getName());
	private SelectionKey register;

	/**
	 * 
	 * TODO: Documentation to be done for constructor 'Acceptor', by 'ACHR'..
	 * 
	 * @throws IOException
	 */
	Acceptor(Dispatcher dispatcher, int serverPort) throws IOException {
		_log.fine("begin");
		
		this.serverPort = serverPort;
		this.dispatcher = dispatcher;
		
		initSelector();
		_log.fine("end");
	}

	public void run() {
		_log.fine("begin");
		while (isRunning) {
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
						_log.finer("selected: "+key);

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
		_log.fine("end");
	}
	
	private void accept(SelectionKey key) throws IOException {
		_log.fine("begin");
		
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel(); // get the key's channeö
		
		SocketChannel clientChannel = serverSocketChannel.accept(); // get the connected client channel
		
//		Socket socket = clientChannel.socket();
		//Utils.debug("Acceptor.accept(): Client: "+socket.getInetAddress());
		clientChannel.configureBlocking(false);
		
//		key.cancel(); // cancel registration on acceptor-selector
		
		dispatcher.registerChannel(clientChannel); // register channel on dispatcher
		
		_log.fine("end");	
	}

	
	/**
	 * Interrupts the acceptor-thread for a server shutdown
	 */
	public void shutdown(){
		_log.fine("begin");
		if (serverChannel!=null){
			try {
				serverChannel.close();
				isRunning = false;
			} catch (IOException e) {	
				// nothing to do
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
		InetSocketAddress isa = new InetSocketAddress("0.0.0.0", serverPort);
		serverChannel.socket().bind(isa);

		register = serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);
		_log.fine("end");
	} 
}
