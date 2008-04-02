package de.root1.simon;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import de.root1.simon.utils.Utils;

public class TxPacket {
	
	private ByteBuffer bb = ByteBuffer.allocate(4096);

	private boolean headerOkay = false;
	private boolean setComplete = false;

	private int bodySize;

	private byte msgType;

	private int requestID;
	
	public void setHeader(byte type, int requestID) {
		this.msgType = type;
		this.requestID = requestID;
		bb.put(type);
		bb.putInt(requestID);
		Utils.debug("TxPacket.setHeader() -> position1="+bb.position());
		bb.position(bb.position()+4); // skip next 4 bytes. they are reserved for the body size
		Utils.debug("TxPacket.setHeader() -> position2="+bb.position());
		headerOkay  = true;
	}
	
	public void putInt(int i){
		if (!headerOkay) throw new IllegalStateException("header not set");
		try {
			bb.putInt(i);
		} catch (BufferOverflowException e){
			bb = Utils.doubleByteBuffer(bb);
			putInt(i);
		}
	}
	
	public void put(byte[] b){
		if (!headerOkay) throw new IllegalStateException("header not set");
		try {
			bb.put(b);
		} catch (BufferOverflowException e){
			bb = Utils.doubleByteBuffer(bb);
			put(b);
		}
	}

	public void setComplete(){
		int pos = bb.position();
		Utils.debug("TxPacket.setComplete() -> position1="+bb.position());
		bb.position(5); // positioniere den Zeiger für das einfügen der Länge des Packet-Bodys
		bodySize = pos-9;
		bb.putInt(bodySize); // Die position - den Header von 9 Bytes ergibt den Body
		bb.position(pos);
		Utils.debug("TxPacket.setComplete() -> position2="+bb.position());
//		bb.rewind();
		bb.flip();
		Utils.debug("TxPacket.setComplete() -> bb="+bb);
		setComplete = true;
	}
	
	public ByteBuffer getByteBuffer(){
		if (!setComplete) throw new IllegalStateException("packet not completed!");
		Utils.debug("TxPacket.getByteBuffer() -> msgType="+msgType+" requestID="+requestID+" bodySize="+bodySize);
		return bb;
	}
	
	
}
