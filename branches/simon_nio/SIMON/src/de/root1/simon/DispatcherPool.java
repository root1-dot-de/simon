/**
 * TODO Documentation to be done
 */
package de.root1.simon;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;

import de.root1.simon.utils.Utils;

/**
 * TODO Documentation to be done
 *
 * @author achristian
 *
 */
public class DispatcherPool {
	
	private Runnable[] dispatcherPool;
	private LookupTable lookupTable;
	
	public DispatcherPool(int poolSize) throws IOException {
		dispatcherPool = new Runnable[poolSize];
		
		for (int i = 0; i < dispatcherPool.length; i++) {
			dispatcherPool[i] = new Dispatcher(lookupTable);
			
		}
	}
	
	/**
	 * 
	 * TODO Documentation to be done
	 * @param channel
	 */
	public void put(SelectableChannel channel){
		Utils.debug("DispatcherPool.put()");
	}

}
