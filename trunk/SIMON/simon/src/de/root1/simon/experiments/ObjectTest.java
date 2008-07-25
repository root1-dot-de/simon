package de.root1.simon.experiments;

import java.io.IOException;
import java.nio.ByteBuffer;

import de.root1.simon.utils.Utils;

public class ObjectTest {
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		
//		Statics.DEBUG_MODE = true;
		
		byte[] stringToBytes = Utils.objectToBytes("Hallo Welt");
		ByteBuffer bb = ByteBuffer.allocate(stringToBytes.length);
		bb.put(stringToBytes);
		
		bb.rewind();
		
		byte[] b = bb.array();
		
		ByteBuffer bb2 = ByteBuffer.allocate(4096);
		bb2.put(b);
		bb2.rewind();
		
		
		String string = (String)Utils.getObject(bb2);
		System.out.println(string);
	}

}
