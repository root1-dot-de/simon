package de.root1.simon.nioexample.interfaces;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.io.IOException;

/**
 * 
 * A smple input queue
 *
 */
public interface IInputQueue {
	
	/**
	 * 
	 * Fills the queue from the given channel
	 * @param channel
	 * @return
	 * @throws IOException
	 */
	int fillFrom(ByteChannel channel) throws IOException;

	/**
	 * 
	 * test if the queue is empty
	 * @return true if empty, false if not
	 */
	boolean isEmpty();
	
	/**
	 * 
	 * TODO Documentation to be done
	 * @param b
	 * @return
	 */
	int indexOf(byte b);
	
	/**
	 * 
	 * get count number of bytes from the queue
	 * @param count
	 * @return
	 */
	ByteBuffer dequeueBytes(int count);
	
	/**
	 * 
	 * discards count number of bytes from the queue
	 * @param count
	 */
	void discardBytes(int count);
}
