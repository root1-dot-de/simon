package de.root1.simon.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class ByteBufferOutputStream extends OutputStream {

	
	ByteBuffer bb = ByteBuffer.allocate(4096);
	
	public void skipBytes(int n){
		bb.position(bb.position()+n);
	}
	
	@Override
	public void write(int b) throws IOException {

		if (bb.position()==bb.limit())
			bb = Utils.doubleByteBuffer(bb);
		
		bb.put((byte)b);
		
	}
	
	@Override
	public void write(byte[] b) throws IOException {
		bb.put(b);
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		bb.put(b, off, len);
	}
	
	public ByteBuffer getByteBuffer(){
		bb.flip();
		return bb;
	}
	
	public int getPosition(){
		return bb.position();
	}

	
	

}
