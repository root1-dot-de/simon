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

import java.nio.charset.Charset;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.root1.simon.codec.messages.AbstractMessage;
import de.root1.simon.codec.messages.MsgError;
import de.root1.simon.codec.messages.MsgHashCodeReturn;
import de.root1.simon.codec.messages.SimonMessageConstants;
import de.root1.simon.utils.Utils;
import java.nio.charset.CharacterCodingException;

/**
 * A {@link MessageDecoder} that decodes {@link MsgHashCodeReturn}.
 *
 * @author ACHR
 */
public class MsgHashCodeReturnDecoder extends AbstractMessageDecoder {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public MsgHashCodeReturnDecoder() {
        super(SimonMessageConstants.MSG_HASHCODE_RETURN);
    }

    @Override
    protected AbstractMessage decodeBody(IoSession session, IoBuffer in) {
        MsgHashCodeReturn message = new MsgHashCodeReturn();
        try {
            message.setReturnValue(in.getInt());
            message.setErrorMsg(in.getPrefixedString(Charset.forName("UTF-8").newDecoder()));
        } catch (CharacterCodingException e) {
            MsgError error = new MsgError();
            error.setErrorMessage("Error while decoding hashCode() return: Not able to read remote object name due to CharacterCodingException.");
            error.setRemoteObjectName(null);
            error.setThrowable(e);
            return error;
        }
        logger.trace("message={}", message);
        return message;
    }

    @Override
    public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
    }
}
