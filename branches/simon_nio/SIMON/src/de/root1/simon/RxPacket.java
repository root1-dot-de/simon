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
