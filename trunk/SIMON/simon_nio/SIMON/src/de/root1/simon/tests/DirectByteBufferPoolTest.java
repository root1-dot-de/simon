package de.root1.simon.tests;

import java.nio.ByteBuffer;


public class DirectByteBufferPoolTest {
	
	public static void main(String[] args) {
		
		ByteBuffer a01 = DirectByteBufferPool.getInstance().getByteBuffer(01);
		ByteBuffer a02 = DirectByteBufferPool.getInstance().getByteBuffer(02);
		ByteBuffer a03 = DirectByteBufferPool.getInstance().getByteBuffer(03);
		ByteBuffer a04 = DirectByteBufferPool.getInstance().getByteBuffer(04);
		ByteBuffer a05 = DirectByteBufferPool.getInstance().getByteBuffer(05);
		
		ByteBuffer b01 = DirectByteBufferPool.getInstance().getByteBuffer(01);
		ByteBuffer b02 = DirectByteBufferPool.getInstance().getByteBuffer(02);
		ByteBuffer b03 = DirectByteBufferPool.getInstance().getByteBuffer(03);
		ByteBuffer b04 = DirectByteBufferPool.getInstance().getByteBuffer(04);
		ByteBuffer b05 = DirectByteBufferPool.getInstance().getByteBuffer(05);
		
		ByteBuffer a11 = DirectByteBufferPool.getInstance().getByteBuffer(11);
		ByteBuffer a12 = DirectByteBufferPool.getInstance().getByteBuffer(12);
		ByteBuffer a13 = DirectByteBufferPool.getInstance().getByteBuffer(13);
		ByteBuffer a14 = DirectByteBufferPool.getInstance().getByteBuffer(14);
		ByteBuffer a15 = DirectByteBufferPool.getInstance().getByteBuffer(15);
		
		ByteBuffer b11 = DirectByteBufferPool.getInstance().getByteBuffer(11);
		ByteBuffer b12 = DirectByteBufferPool.getInstance().getByteBuffer(12);
		ByteBuffer b13 = DirectByteBufferPool.getInstance().getByteBuffer(13);
		ByteBuffer b14 = DirectByteBufferPool.getInstance().getByteBuffer(14);
		ByteBuffer b15 = DirectByteBufferPool.getInstance().getByteBuffer(15);

		System.out.println("A-------------------------------------------------------");
		
		DirectByteBufferPool.getInstance().releaseByteBuffer(a01);
		DirectByteBufferPool.getInstance().releaseByteBuffer(a02);
		DirectByteBufferPool.getInstance().releaseByteBuffer(a03);
		DirectByteBufferPool.getInstance().releaseByteBuffer(a04);
		DirectByteBufferPool.getInstance().releaseByteBuffer(a05);
		
		DirectByteBufferPool.getInstance().releaseByteBuffer(b01);
		DirectByteBufferPool.getInstance().releaseByteBuffer(b02);
		DirectByteBufferPool.getInstance().releaseByteBuffer(b03);
		DirectByteBufferPool.getInstance().releaseByteBuffer(b04);
		DirectByteBufferPool.getInstance().releaseByteBuffer(b05);
		
		System.out.println("B-------------------------------------------------------");
		
		DirectByteBufferPool.getInstance().releaseByteBuffer(b01);
		DirectByteBufferPool.getInstance().releaseByteBuffer(b02);
		DirectByteBufferPool.getInstance().releaseByteBuffer(b03);
		DirectByteBufferPool.getInstance().releaseByteBuffer(b04);
		DirectByteBufferPool.getInstance().releaseByteBuffer(b05);
		
		DirectByteBufferPool.getInstance().releaseByteBuffer(b11);
		DirectByteBufferPool.getInstance().releaseByteBuffer(b12);
		DirectByteBufferPool.getInstance().releaseByteBuffer(b13);
		DirectByteBufferPool.getInstance().releaseByteBuffer(b14);
		DirectByteBufferPool.getInstance().releaseByteBuffer(b15);
		
	}

}
