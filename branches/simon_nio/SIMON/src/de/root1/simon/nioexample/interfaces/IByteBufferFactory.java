package de.root1.simon.nioexample.interfaces;

import java.nio.ByteBuffer;

/**
 * TODO Documentation to be done
 */
public interface IByteBufferFactory {
	
	/**
	 * 
	 * TODO Documentation to be done
	 * @return
	 */
	ByteBuffer newBuffer();
	
	/**
	 * 
	 * TODO Documentation to be done
	 * @param buffer
	 */
	void returnBuffer(ByteBuffer buffer);
}
