package de.root1.simon.nioexample.impl;

import java.nio.ByteBuffer;

import de.root1.simon.nioexample.interfaces.IByteBufferFactory;

public class DumbBufferFactory implements IByteBufferFactory {
	private int capacity;

	public DumbBufferFactory(int capacity) {
		this.capacity = capacity;
	}

	public ByteBuffer newBuffer() {
		return (ByteBuffer.allocate(capacity));
	}

	public void returnBuffer(ByteBuffer buffer) {
		// do nothing
	}
}
