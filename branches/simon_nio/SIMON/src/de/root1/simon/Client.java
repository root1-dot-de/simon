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
import java.util.logging.Logger;

import de.root1.simon.exceptions.EstablishConnectionFailed;
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
	
	protected Logger _log = Logger.getLogger(this.getClass().getName());
	
	/**
	 * TODO Documentation to be done
	 * @throws IOException 
	 */
	public Client(Dispatcher dispatcher) throws IOException {
		_log.fine("begin");
		this.dispatcher = dispatcher;
		_log.fine("end");
	}
	
	public void connect(String host, int port) throws IOException, EstablishConnectionFailed{
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
					throw new EstablishConnectionFailed("could not establish connectionto server. is server running? error-msg:"+e);
					
				}
				
			} else throw new IllegalStateException("invalid op event: op="+key.interestOps());
			
		}
		
		_log.fine("end");
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