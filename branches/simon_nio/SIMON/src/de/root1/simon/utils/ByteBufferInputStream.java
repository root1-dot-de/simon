package de.root1.simon.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ByteBufferInputStream extends InputStream {

	
	private ByteBuffer bb;
	
	public ByteBufferInputStream(ByteBuffer bb) {
		this.bb = bb;
	}
	
	public synchronized int read() {
		return bb.get();
	}
	
	public int read(byte[] b) throws IOException {
		bb.get(b);
		return b.length;
	}
	
	public synchronized int read(byte[] b, int off, int len) {
		bb.get(b, off, len);
		return len;
	}
	
	public synchronized int available() {
		return bb.limit()-bb.position();
	}

	public boolean markSupported() {
		return false;
	}
	
	public synchronized void reset() {
	}
	
	public void mark(int readAheadLimit) {
	}
	
	public synchronized long skip(long n) {
		bb.position((int)(bb.position()+n));
		return n;
	}
	
	
	
	
	
	

}
