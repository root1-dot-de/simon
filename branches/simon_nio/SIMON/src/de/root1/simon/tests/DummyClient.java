package de.root1.simon.tests;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import de.root1.simon.Statics;
import de.root1.simon.utils.Utils;

public class DummyClient {
	
	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
		Socket s = new Socket("localhost",2000);
		
		OutputStream outputStream = s.getOutputStream();
		
		int packetLength = 4+4+4+6;
		
		byte[] b = new byte[19];
		
		b[0] = Statics.LOOKUP_PACKET;
		System.arraycopy(Utils.integerToBytes(packetLength), 0, b, 1, 4);
		System.arraycopy(Utils.integerToBytes(99), 0, b, 5, 4);
		
		System.arraycopy(Utils.stringToBytes("server"), 0, b, 9, "server".length()+4);
		
		for (int i = 0; i < b.length; i++) {
			byte c = b[i];
			
			System.out.println("b["+i+"]="+c);
			
		}
		
		
		
		
		outputStream.write(b);
		outputStream.flush();
		Thread.sleep(100000);
	}
	

}
