package de.root1.simon;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;

import de.root1.simon.utils.Utils;

public class Acceptor implements Runnable {

	private int serverPort = 2000;
	private boolean isRunning = true;
	private ServerSocketChannel serverChannel;
	private DispatcherPool dispatcherPool;
	private Selector socketSelector;

	Acceptor() throws IOException {
	
		Utils.debug("Acceptor.Acceptor() -> init ...");
		
		dispatcherPool = new DispatcherPool(1);
		
		socketSelector = SelectorProvider.provider().openSelector();
		// Create a new non-blocking server socket channel
		this.serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);

		// Bind the server socket to the specified address and port
		InetSocketAddress isa = new InetSocketAddress("0.0.0.0", serverPort);
		serverChannel.socket().bind(isa);

		// Register the server socket channel, indicating an interest in 
		// accepting new connections
		serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);
		
	}

	public void run() {
		    while (true) {
		      try {
		    	  
		    	  Utils.debug("Acceptor.run() -> Selecting ...");
		        // Wait for an event one of the registered channels
		        this.socketSelector.select();

		        // Iterate over the set of keys for which events are available
		        Iterator selectedKeys = this.socketSelector.selectedKeys().iterator();
		        while (selectedKeys.hasNext()) {
		          SelectionKey key = (SelectionKey) selectedKeys.next();
		          selectedKeys.remove();

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
	}
	
	private void accept(SelectionKey key) throws IOException {
		Utils.debug("Acceptor.accept(): Start");
		
		SelectableChannel channel = (SelectableChannel) key.channel(); // get the key's channeö
		key.cancel(); // cancel registration on acceptor-selector
		dispatcherPool.put(channel);  // put to dispatcher-pool
		
		Utils.debug("Acceptor.accept(): End");
	}

	
	/**
	 * Interrupts the acceptor-thread for a server shutdown
	 */
	public void shutdown(){
		if (serverChannel!=null){
			try {
				serverChannel.close();
				isRunning = false;
			} catch (IOException e) {	
				// nothing to do
			}
		}
	}
	
	  private Selector initSelector() throws IOException {
		    // Create a new selector
		    Selector socketSelector = SelectorProvider.provider().openSelector();

		    // Create a new non-blocking server socket channel
		    this.serverChannel = ServerSocketChannel.open();
		    serverChannel.configureBlocking(false);

		    // Bind the server socket to the specified address and port
		    InetSocketAddress isa = new InetSocketAddress("0.0.0.0", this.serverPort);
		    serverChannel.socket().bind(isa);

		    // Register the server socket channel, indicating an interest in 
		    // accepting new connections
		    serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);

		    return socketSelector;
		  } 
}
