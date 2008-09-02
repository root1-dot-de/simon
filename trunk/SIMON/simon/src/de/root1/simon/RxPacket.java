/*
 * Copyright (C) 2008 Alexander Christian <alex(at)root1.de>. All rights reserved.
 * 
 * This file is part of SIMON.
 *
 *   SIMON is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SIMON is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with SIMON.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.root1.simon;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.root1.simon.exceptions.PacketCorruptedException;

/**
 * Simple class that holds all bytes received with a packet by SIMON.
 * @author alexanderchristian
 *
 */
public class RxPacket {
	
	protected transient Logger _log = Logger.getLogger(this.getClass().getName());
	
	/** a 11 byte big header, holding the request id, message type, the size of the body and some additional bytes to identify a simon packet */
	private  ByteBuffer header;

	/** the message type, see fields in {@link Statics} */
	private  byte msgType;
	
	/** the request id of the received packet */
	private  int requestID;
	
	/** the body contains additional information for the packet */
	private  ByteBuffer body;
	
	/** the size of the body in bytes */
	private int bodySize;
	
	/**
	 * Creates a new instance of {@link RxPacket}, which suddenly reads all needed bytes to get the packet.
	 * @param socketChannel the channel to read from
	 * @throws IOException if there is a problem with the network connection
	 * @throws PacketCorruptedException 
	 */
	RxPacket(SocketChannel socketChannel) throws IOException, PacketCorruptedException {
		
		header = ByteBuffer.allocate(11);
		
		int headerRead = 0;
		
		while (headerRead!=11) {
			headerRead += socketChannel.read(header);
			if (_log.isLoggable(Level.FINEST)){
				_log.finest("header: read "+headerRead+" of 11 bytes");
			}
			
			if (headerRead<0) {
				_log.fine("No data read. socketChannel="+socketChannel);
				throw new IOException("could not read header with 11 bytes. maybe connection is broken. header.limit()="+header.limit());
			}
		}
		
		header.rewind();
		
		
		
		int simonPacketHeaderId0 = header.get();
		int simonPacketHeaderId1 = header.get();
		
		msgType = header.get();
		requestID = header.getInt();
		bodySize = header.getInt();

		if (simonPacketHeaderId0!=Statics.SIMON_PACKET_HEADER_ID0 ||
			 simonPacketHeaderId1!=Statics.SIMON_PACKET_HEADER_ID1) {
			
			// FIXME what to do if header is wrong?
			_log.severe("packet header failure! "+this+": Exiting system.  header_id0=0x"+Integer.toHexString(simonPacketHeaderId0)+"  " +
					"header_id1=0x"+Integer.toHexString(simonPacketHeaderId1)+" "+
					"msgType="+Integer.toHexString(msgType)+" "+
					"requestID="+Integer.toHexString(requestID)+" "+
					"bodySize="+Integer.toHexString(bodySize));
			
			header.rewind();
			String headerError = "";
			for (int i=0;i<11;i++){
				 headerError += "\theader b["+i+"]="+header.get()+"\n";
				
			}
			_log.severe("error header bytes: \n"+headerError);
			
			
			throw new PacketCorruptedException("the received packet is currupted. See logging output.");
		}

		if (_log.isLoggable(Level.FINEST)){
			_log.finest("header: headerId0=0x"+Integer.toHexString(simonPacketHeaderId0)+" headerId1=0x"+Integer.toHexString(simonPacketHeaderId1)+" msgType="+msgType+" requestID="+requestID+" bodySize="+bodySize);
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
		
		_log.fine("finished reading body.");
	}

	/**
	 * TODO document me
	 * @return
	 */
	public  byte getMsgType() {
		return msgType;
	}

	/**
	 * TODO document me
	 * @return
	 */
	public  int getRequestID() {
		return requestID;
	}

	/**
	 * TODO document me
	 * @return
	 */
	public  ByteBuffer getBody() {
		return body;
	}

	/**
	 * TODO document me
	 * @return
	 */
	public ByteBuffer getByteBuffer() {

		header.rewind();
		body.rewind();
		
		ByteBuffer b = ByteBuffer.allocate(11+bodySize);
		b.put(header);
		b.put(body);
		
		b.rewind();
		
		body.rewind();
		header.rewind();
		
		return b;
	}

}
