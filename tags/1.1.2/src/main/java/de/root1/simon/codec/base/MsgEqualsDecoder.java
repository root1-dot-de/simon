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
import de.root1.simon.codec.messages.MsgEquals;
import de.root1.simon.codec.messages.MsgError;
import de.root1.simon.codec.messages.SimonMessageConstants;

/**
 * A {@link MessageDecoder} that decodes {@link MsgEquals}.
 *
 * @author ACHR
 */
public class MsgEqualsDecoder extends AbstractMessageDecoder {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public MsgEqualsDecoder() {
        super(SimonMessageConstants.MSG_EQUALS);
    }

    @Override
    protected AbstractMessage decodeBody(IoSession session, IoBuffer in) {

        MsgEquals message = new MsgEquals();
        String remoteObjectName = null;
        try {

            remoteObjectName = in.getPrefixedString(Charset.forName("UTF-8").newDecoder());
            Object objectToCompareWith = in.getObject();
            message.setRemoteObjectName(remoteObjectName);
            message.setObjectToCompareWith(objectToCompareWith);
            
        } catch (CharacterCodingException e) {
            
            MsgError error = new MsgError();
            error.setErrorMessage("Error while decoding equals() request: Not able to read remote object name due to CharacterCodingException.");
            error.setRemoteObjectName(remoteObjectName);
            error.setThrowable(e);
            return error;

        } catch (ClassNotFoundException e) {
            MsgError error = new MsgError();
            error.setErrorMessage("Error while decoding equals() request: Not able to read object due to ClassNotFoundException.");
            error.setRemoteObjectName(remoteObjectName);
            error.setThrowable(e);
            return error;
        }
        logger.debug("message={}", message);
        return message;
    }

    @Override
    public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
    }
}
