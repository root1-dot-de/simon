package de.root1.simon;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

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
		bb.position(bb.position()+4); // skip next 4 bytes. they are reserved for the body size
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
	
	public void putLong(Long l){
		if (!headerOkay) throw new IllegalStateException("header not set");
		try {
			bb.putLong(l);
		} catch (BufferOverflowException e){
			bb = Utils.doubleByteBuffer(bb);
			putLong(l);
		}
	}
	
	public void putChar(char c){
		if (!headerOkay) throw new IllegalStateException("header not set");
		try {
			bb.putChar(c);
		} catch (BufferOverflowException e){
			bb = Utils.doubleByteBuffer(bb);
			putChar(c);
		}
	}
	
	public void putShort(short s){
		if (!headerOkay) throw new IllegalStateException("header not set");
		try {
			bb.putShort(s);
		} catch (BufferOverflowException e){
			bb = Utils.doubleByteBuffer(bb);
			putShort(s);
		}
	}
	
	public void putFloat(float f){
		if (!headerOkay) throw new IllegalStateException("header not set");
		try {
			bb.putFloat(f);
		} catch (BufferOverflowException e){
			bb = Utils.doubleByteBuffer(bb);
			putFloat(f);
		}
	}
	
	public void putDouble(double d){
		if (!headerOkay) throw new IllegalStateException("header not set");
		try {
			bb.putDouble(d);
		} catch (BufferOverflowException e){
			bb = Utils.doubleByteBuffer(bb);
			putDouble(d);
		}
	}
	
	public void put(byte[] b){
		if (!headerOkay) throw new IllegalStateException("header not set");
//		try {
			
			if (bb.limit()-bb.position()<b.length){
				bb = Utils.doubleByteBuffer(bb);
				put(b);
			}
			
			bb.put(b);
//		} catch (BufferOverflowException e){
//			bb = Utils.doubleByteBuffer(bb);
//			put(b);
//		}
	}
	
	public void put(byte b){
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
		//Utils.debug("TxPacket.setComplete() -> position1="+bb.position());
		bb.position(5); // positioniere den Zeiger für das einfügen der Länge des Packet-Bodys
		bodySize = pos-9;
		bb.putInt(bodySize); // Die position - den Header von 9 Bytes ergibt den Body
		bb.position(pos);
		//Utils.debug("TxPacket.setComplete() -> position2="+bb.position());
//		bb.rewind();
		bb.flip();
		//Utils.debug("TxPacket.setComplete() -> bb="+bb);
		setComplete = true;
	}
	
	public ByteBuffer getByteBuffer(){
		if (!setComplete) throw new IllegalStateException("packet not completed!");
		//Utils.debug("TxPacket.getByteBuffer() -> msgType="+msgType+" requestID="+requestID+" bodySize="+bodySize);
		return bb;
	}
	
	public int getBodySize(){
		return bodySize;
	}
	
	public int getRequestID(){
		return requestID;
	}
	
	public byte getMsgType(){
		return msgType;
	}
	
}
