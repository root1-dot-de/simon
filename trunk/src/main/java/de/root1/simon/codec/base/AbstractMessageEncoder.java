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
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.demux.MessageEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.root1.simon.codec.messages.AbstractMessage;
import de.root1.simon.codec.messages.MsgError;
import de.root1.simon.codec.messages.SimonMessageConstants;
import de.root1.simon.exceptions.SimonException;

/**
 * A {@link MessageEncoder} that encodes message header and forwards
 * the encoding of body to a subclass.
 *
 * @author ACHR
 */
public abstract class AbstractMessageEncoder<T extends AbstractMessage> implements MessageEncoder<T> {
	
    @SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private MsgError msgError = null;

    @Override
    public void encode(IoSession session, T message, ProtocolEncoderOutput out) throws Exception {
        
        IoBuffer buf = null;
        try {
        
            buf = putMessageToBuffer(session, message);
        
        } catch (Throwable t) {

            if (buf!=null) {
                buf.clear();
            }

            // form an error message
            MsgError error = new MsgError();
            error.setErrorMessage("Error while encoding message. sequence="+message.getSequence()+" type="+(message.getMsgType()==-1?"{unknown}":message.getMsgType()));
            error.setInitSequenceId(message.getSequence());
            error.setEncodeError();
            
            // change type to error;
//            msgType = SimonMessageConstants.MSG_ERROR;
            
            // put the message into the buffer
            buf = putMessageToBuffer(session, message);
            msgError = error;
        }
        
        // send the buffer
        out.write(buf);
        
        if (msgError!=null) {
            session.close(false);
            String exceptionMessage = "";
            String remoteObjectName = msgError.getRemoteObjectName();
            String errorMessage = msgError.getErrorMessage();
            Throwable throwable = msgError.getThrowable();
            
            if (remoteObjectName!=null && remoteObjectName.length()>0) {
                exceptionMessage = "An error occured on remote while writing a message to remote object '"+remoteObjectName+"'. Error message: "+errorMessage;
            } else {
                exceptionMessage = "An error occured on remote while writing a message. Error message: "+errorMessage;
            }
            
            SimonException se = new SimonException(exceptionMessage);
            se.initCause(throwable);
        }
    }
    
    /**
     * put message + message header into a single enclosed buffer
     * 
     * @param session
     * @param message
     * @return complete message in a buffer
     */
    private IoBuffer putMessageToBuffer(IoSession session, T message) {
        
        // Encode the message body
        IoBuffer msgBuffer = IoBuffer.allocate(16);
        msgBuffer.setAutoExpand(true);
        encodeBody(session, message, msgBuffer);

        IoBuffer buf = IoBuffer.allocate(SimonMessageConstants.HEADER_LEN+msgBuffer.position() /* position = length of message in this case */);
        
        // Encode the header
        buf.put(message.getMsgType()); // header contains message type
        buf.putInt(message.getSequence()); // header contains sequence
        buf.putInt(msgBuffer.position()); // and header contains length of message
        logger.trace("Sending msg type [{}] with sequence [{}] and bodysize [{}] to next layer ...", new Object[]{message.getMsgType(), message.getSequence(),msgBuffer.position()});

        msgBuffer.flip();
        buf.put(msgBuffer); // after the header, the message is sent
        
        buf.flip();
        
        return buf;
    }

    /**
     * Encodes the body of the message.
     * This method has to be implemented by the message encoder class that extends this class
     * @param session
     * @param message
     * @param out
     */
    protected abstract void encodeBody(IoSession session, T message, IoBuffer out);
    
    /**
     * This method is called by an Encoder class in case of an exception:
     * The encoder class gathers all available error information, put them into an 
     * {@link MsgError} message and calls this method.
     * This method clean the buffer and replaces the content with the error message
     * 
     * @param out the "out" buffer used by the encoder class to store data to be sent
     * @param session the assiciated session
     * @param error the error message
     */
    void sendEncodingError(IoBuffer out, IoSession session, MsgError error) {
        out.clear();
        MsgErrorEncoder mee = new MsgErrorEncoder();
        mee.encodeBody(session, error, out);
        msgError = error;
    }
}
