/**
 * TODO Documentation to be done
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

import de.root1.simon.utils.Utils;

/**
 * TODO Documentation to be done
 *
 * @author achristian
 *
 */
public class Client {
	
	private Dispatcher dispatcher;
	private SocketChannel clientSocketChannel;
	private Selector selector;
	private SelectionKey key;
	private int requestIdCounter;
	
	/**
	 * TODO Documentation to be done
	 * @throws IOException 
	 */
	public Client(Dispatcher dispatcher) throws IOException {
		
		this.dispatcher = dispatcher;
		
	}
	
	public void connect(String host, int port) throws IOException{
		
		selector = SelectorProvider.provider().openSelector();
		//Utils.debug("Client.connect() -> start");
		clientSocketChannel = SocketChannel.open();
		clientSocketChannel.configureBlocking(false);
	
		// Kick off connection establishment
		clientSocketChannel.connect(new InetSocketAddress(host, port));
	
		// Queue a channel registration since the caller is not the 
		// selecting thread. As part of the registration we'll register
		// an interest in connection events. These are raised when a channel
		// is ready to complete connection establishment.
		
//		SelectionKey clientKey = 
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
					//Utils.debug("Client.connect() -> finishing connection");
					socketChannel.finishConnect();
					//Utils.debug("Client.connect() -> register on dispatcher");
					dispatcher.registerChannel(socketChannel);
					
				} catch (IOException e) {
					// Cancel the channel's registration with our selector
					System.err.println("Exception in finishConnection(): "+e);
					key.cancel();
					return;
				}
				
			} else throw new IllegalStateException("invalid op event: op="+key.interestOps());
			
		}
		
		//Utils.debug("Client.connect() -> end");
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

	public SelectionKey getKey() {
		return key;
	}
	
	public SelectableChannel getChannelToServer() {
		return key.channel();
	}
	
	
	

	

}
