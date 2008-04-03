package de.root1.simon.nioexample.interfaces;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.io.IOException;

public interface IOutputQueue {
	boolean isEmpty();
	int drainTo(ByteChannel channel) throws IOException;

	boolean enqueue(ByteBuffer byteBuffer);
}
