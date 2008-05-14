package de.root1.simon.tests;

import java.nio.ByteBuffer;

import de.root1.simon.Statics;
import de.root1.simon.utils.Utils;

public class StringTest {
	
	public static void main(String[] args) {
		
		ByteBuffer b = ByteBuffer.allocate(4);
		
		b.putInt(123456789);
		byte[] array = b.array();
		
		System.out.println("b[0]="+array[0]);
		System.out.println("b[1]="+array[1]);
		System.out.println("b[2]="+array[2]);
		System.out.println("b[3]="+array[3]);
		
		System.out.println("-------");
		
		byte[] x = new byte[4];
		int v = 123456789;
		x[0] = (byte)((v >>> 24) & 0xFF);
		x[1] = (byte)((v >>> 16) & 0xFF);
	    x[2] = (byte)((v >>>  8) & 0xFF);
	    x[3] = (byte)((v >>>  0) & 0xFF);
	    
	    System.out.println("b[0]="+x[0]);
		System.out.println("b[1]="+x[1]);
		System.out.println("b[2]="+x[2]);
		System.out.println("b[3]="+x[3]);
		
		System.out.println("-------");
		
		byte[] stringToBytes = Utils.stringToBytes("Hallo Welt");
		ByteBuffer bb = ByteBuffer.allocate(stringToBytes.length);
		bb.put(stringToBytes);
		
		bb.rewind();
		
		String string = Utils.getString(bb);
		System.out.println(string);
	}

}
