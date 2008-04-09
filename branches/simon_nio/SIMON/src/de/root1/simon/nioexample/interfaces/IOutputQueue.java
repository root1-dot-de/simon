package de.root1.simon.nioexample.interfaces;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

/**
 * 
 * A simple output queue
 *
 * @author achristian
 *
 */
public interface IOutputQueue {
	
	/**
	 * 
	 * Test if the queue is empty
	 * @return true is empty, false if not
	 */
	boolean isEmpty();
	
	/**
	 * 
	 * Drains the queue to the given channel
	 * @param channel
	 * @return
	 * @throws IOException
	 */
	int drainTo(ByteChannel channel) throws IOException;

	/**
	 * 
	 * Enqueue a given ByteBuffer
	 * @param byteBuffer
	 * @return false if the buffer's remaining()==0, otherwise true
	 */
	boolean enqueue(ByteBuffer byteBuffer);
	
}
