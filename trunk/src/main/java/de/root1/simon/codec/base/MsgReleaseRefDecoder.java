/*
 * Copyright (C) 2012 Alexander Christian <alex(at)root1.de>. All rights reserved.
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

import de.root1.simon.codec.messages.AbstractMessage;
import de.root1.simon.codec.messages.MsgPing;
import de.root1.simon.codec.messages.MsgReleaseRef;
import de.root1.simon.codec.messages.SimonMessageConstants;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link MessageDecoder} that decodes {@link MsgPing}.
 *
 * @author ACHR
 */
public class MsgReleaseRefDecoder extends AbstractMessageDecoder {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public MsgReleaseRefDecoder() {
        super(SimonMessageConstants.MSG_RELEASE_REF);
    }

    @Override
    protected AbstractMessage decodeBody(IoSession session, IoBuffer in) {

        logger.trace("begin");
        String refId;
        MsgReleaseRef msg = new MsgReleaseRef();
        try {
            refId = in.getPrefixedString(Charset.forName("UTF-8").newDecoder());
            msg.setSequence(getCurrentSequence());
            msg.setRefId(refId);
        } catch (CharacterCodingException ex) {
            logger.warn("Error decoding release ref: ",ex);
        }
        logger.trace("end");
        return msg;
    }

    @Override
    public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
    }
}
