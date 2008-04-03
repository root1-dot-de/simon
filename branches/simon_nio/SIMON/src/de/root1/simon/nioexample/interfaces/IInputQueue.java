package de.root1.simon.nioexample.interfaces;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.io.IOException;

public interface IInputQueue {
	int fillFrom(ByteChannel channel) throws IOException;

	boolean isEmpty();
	int indexOf(byte b);
	ByteBuffer dequeueBytes(int count);
	void discardBytes(int count);
}
