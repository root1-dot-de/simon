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

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.root1.simon.codec.messages.AbstractMessage;
import de.root1.simon.codec.messages.MsgError;
import de.root1.simon.codec.messages.SimonMessageConstants;

/**
 * A {@link MessageDecoder} that decodes message header and forwards
 * the decoding of body to a subclass.
 *
 * @author achr
 */
public abstract class AbstractMessageDecoder implements MessageDecoder {
	
    @SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.getLogger(getClass());
	
    private final byte msgType;
    private int sequence;
    private int bodysize;

    private boolean readHeader;
    
    /**
     * Creates a new message decoder
     * @param msgType specifies a unique ID for the type of message
     */
    protected AbstractMessageDecoder(byte msgType) {
        this.msgType = msgType;
    }

    @Override
    public MessageDecoderResult decodable(IoSession session, IoBuffer in) {

        // Return NEED_DATA if the whole header is not yet available
        if (in.remaining() < SimonMessageConstants.HEADER_LEN) {
            logger.trace("Header not received completely. Right now we have {} of {} bytes", in.remaining(), SimonMessageConstants.HEADER_LEN);
            return MessageDecoderResult.NEED_DATA;
        }
        // Return OK if THIS decoder is correct type to decode the message
        int type = in.get();
        if (msgType == type) {
            return MessageDecoderResult.OK;
        }

        // Return NOT_OK if THIS decoder isn't able to decode THIS message
        return MessageDecoderResult.NOT_OK;
    }

    @Override
    public MessageDecoderResult decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        // Try to skip header if not read.
        if (!readHeader) {
            in.get(); // Skip 'msgType'.
            sequence = in.getInt(); // Get 'sequence'.
            bodysize = in.getInt(); // Get the body's size
            readHeader = true;
        }

        // check if the complete message body is available
        if (in.remaining()<bodysize) {
            logger.trace("Message type [{}] with sequence [{}] needs [{}] bytes. Right now we only have [{}]. Waiting for more ...", new Object[]{msgType, sequence, bodysize, in.remaining()});
            return MessageDecoderResult.NEED_DATA;
        } else {
            logger.trace("Message type [{}] with sequence [{}] with [{}] bytes body size is available. Now decoding ...", new Object[]{msgType, sequence, bodysize});
        }

        // catch all errors/exceptions/problems which are not handled by decodeBody
        try {
            // Try to decode body
            AbstractMessage m = decodeBody(session, in);
            // Return NEED_DATA if the body is not fully read.
            if (m == null) {
                return MessageDecoderResult.NEED_DATA;
            } else {
                readHeader = false; // reset readHeader for the next decode
            }
            m.setSequence(sequence);
            logger.trace("finished decoding complete message: {}. Forwarding to next layer ...",m);
            out.write(m);
            return MessageDecoderResult.OK;
        } catch (Throwable t) {
            
            // gather all available information an inform the remote side about the problem
            MsgError m = new MsgError();
            m.setErrorMessage("Error while decoding message. sequence="+sequence+" bodySize="+bodysize+" type="+(msgType==-1?"{unknown}":msgType));
            m.setRemoteObjectName(null);
            m.setThrowable(t);
            try {
                session.write(m);
            } catch (Throwable tt) {
                logger.warn("Not able to send error message to remote: "+m);
            }
            
        }
        
        return MessageDecoderResult.NOT_OK;
    }

    /**
     * @return <tt>null</tt> if the whole body is not read yet
     */
    protected abstract AbstractMessage decodeBody(IoSession session, IoBuffer in);
    
    protected int getCurrentSequence(){
    	return sequence;
    }

    protected int getBodySize(){
        return bodysize;
    }
}
