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
package de.root1.simon.codec.base;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.root1.simon.codec.messages.AbstractMessage;
import de.root1.simon.codec.messages.MsgHashCode;
import de.root1.simon.codec.messages.SimonMessageConstants;

/**
 * A {@link MessageDecoder} that decodes {@link MsgHashCode}.
 *
 * @author ACHR
 */
public class MsgHashCodeDecoder extends AbstractMessageDecoder {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
    public MsgHashCodeDecoder() {
        super(SimonMessageConstants.MSG_HASHCODE);
    }
    
    @Override
    protected AbstractMessage decodeBody(IoSession session, IoBuffer in) {

    	MsgHashCode message = new MsgHashCode();
    	
        try {
        	
	        	String remoteObjectName = in.getPrefixedString(Charset.forName("UTF-8").newDecoder());
	        	
	        	message.setRemoteObjectName(remoteObjectName);
		} catch (CharacterCodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		logger.trace("message={}", message);
        return message;
    }
    
    public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
    }
    
   
}
