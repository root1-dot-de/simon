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
	
	RxPacket(SocketChannel socketChannel) throws IOException {
		header = ByteBuffer.allocate(9);
		int headerRead = 0;
		while (headerRead!=9) {
			headerRead += socketChannel.read(header);
			Utils.debug("RxPacket.RxPacket() -> read "+headerRead+" of 9 bytes");
		}
		header.rewind();
		msgType = header.get();
		requestID = header.getInt();
		int bodyLength = header.getInt();
		body = ByteBuffer.allocate(bodyLength);
		
		int bodyRead = 0;
		while (bodyRead!=bodyLength) {
			bodyRead += socketChannel.read(body);
			Utils.debug("RxPacket.RxPacket() -> read "+bodyRead+" of "+bodyLength+" bytes");
		}
	}

	public  byte getMsgType() {
		return msgType;
	}

	public  int getRequestID() {
		return requestID;
	}

	public  ByteBuffer getBody() {
		body.rewind();
		body.flip();
		return body;
	}

}
