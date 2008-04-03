package de.root1.simon.nioexample.interfaces;

import java.io.IOException;
import java.nio.channels.SelectableChannel;

/**
 * 
 * TODO Documentation to be done
 *
 */
public interface IDispatcher {
	
	/**
	 * 
	 * TODO Documentation to be done
	 * @throws IOException
	 */
	void dispatch() throws IOException;

	/**
	 * 
	 * TODO Documentation to be done
	 */
	void shutdown();

	/**
	 * 
	 * registers a given channel with the dispatcher
	 * @param channel the channel to register
	 * @param handler the handler to be associated with the channel
	 * @return
	 * @throws IOException
	 */
	IChannelFacade registerChannel(SelectableChannel channel, IInputHandler handler) throws IOException;

	/**
	 * 
	 * unregisters a given key from the dispatcher
	 * @param key
	 */
	void unregisterChannel(IChannelFacade key);
}
