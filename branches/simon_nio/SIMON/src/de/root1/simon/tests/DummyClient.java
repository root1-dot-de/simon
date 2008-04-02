package de.root1.simon.tests;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import de.root1.simon.Statics;
import de.root1.simon.TxPacket;
import de.root1.simon.utils.Utils;

public class DummyClient {
	
	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {

		Socket s = new Socket("localhost",2000);
		OutputStream outputStream = s.getOutputStream();
		
		TxPacket p = new TxPacket();
		
		p.setHeader(Statics.LOOKUP_PACKET, 0);
		p.put(Utils.stringToBytes("server"));
		p.setComplete();
		p.getByteBuffer();
		
		System.out.println("msgType="+p.getMsgType());
		System.out.println("requestID="+p.getRequestID());
		System.out.println("bodySize="+p.getBodySize());

		
		byte[] b = p.getByteBuffer().array();
				
		for (int i = 0; i < 9; i++) {
			byte c = b[i];
			
			System.out.println("header b["+i+"]="+c);
			
		}
		
		for (int i = 9; i < 9+p.getBodySize(); i++) {
			byte c = b[i];
			
			System.out.println("body   b["+i+"]="+c);
			
		}
		
		
		
		outputStream.write(b);
		outputStream.flush();
		
		
		InputStream inputStream = s.getInputStream();
		
		byte[] rxb = new byte[117];
		
		inputStream.read(rxb);
		
		for (int i = 0; i < 9; i++) {
			byte c = rxb[i];
			System.out.println("rx header b["+i+"]="+c);
		}
		for (int i = 9; i < rxb.length; i++) {
			byte c = rxb[i];
			System.out.println("rx body b["+(i-9)+"]="+c);
		}
//		inputStream.close();
//		s.close();
		
		Thread.sleep(100000);
	}
	

}
