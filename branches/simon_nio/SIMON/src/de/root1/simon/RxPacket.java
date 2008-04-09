package de.root1.simon;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import de.root1.simon.utils.Utils;

public class RxPacket {
	
	private  ByteBuffer header;
	private  byte msgType;
	private  int requestID;
	private  ByteBuffer body;
	private int bodySize;
	
	RxPacket(SocketChannel socketChannel) throws IOException {
		header = ByteBuffer.allocate(9);
		int headerRead = 0;
		while (headerRead!=9) {
			headerRead += socketChannel.read(header);
			//Utils.debug("RxPacket.RxPacket() -> header: read "+headerRead+" of 9 bytes");
		}
		header.rewind();
		
		msgType = header.get();
		requestID = header.getInt();
		bodySize = header.getInt();
		
		body = ByteBuffer.allocate(bodySize);
		
		int bodyRead = 0;
		while (bodyRead!=bodySize) {
			bodyRead += socketChannel.read(body);
			//Utils.debug("RxPacket.RxPacket() -> body: read "+bodyRead+" of "+bodySize+" bytes");
		}
		body.rewind();
		//Utils.debug("RxPacket.RxPacket() -> got complete packet ... msgType="+msgType+" requestID="+requestID+" bodySize="+bodySize);
	}

	public  byte getMsgType() {
		return msgType;
	}

	public  int getRequestID() {
		return requestID;
	}

	public  ByteBuffer getBody() {
		return body;
	}

}
