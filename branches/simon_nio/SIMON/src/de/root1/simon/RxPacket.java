package de.root1.simon;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.root1.simon.utils.Utils;

public class RxPacket {
	
	protected transient Logger _log = Logger.getLogger(this.getClass().getName());
	
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
			if (_log.isLoggable(Level.FINEST)){
				_log.finest("header: read "+headerRead+" of 9 bytes");
			}
		}
		
		header.rewind();
		
		msgType = header.get();
		requestID = header.getInt();
		bodySize = header.getInt();

		if (_log.isLoggable(Level.FINEST)){
			_log.finest("header: msgType="+msgType+" requestID="+requestID+" bodySize="+bodySize);
		}	
		
		body = ByteBuffer.allocate(bodySize);
		
		int bodyRead = 0;
		while (bodyRead!=bodySize) {
			bodyRead += socketChannel.read(body);
			if (_log.isLoggable(Level.FINEST)){
				_log.finest("body: read "+bodyRead+" of "+bodySize+" bytes");
			}
		}
		body.rewind();
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
