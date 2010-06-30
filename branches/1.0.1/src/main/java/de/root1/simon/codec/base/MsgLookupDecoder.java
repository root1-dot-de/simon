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

import de.root1.simon.Statics;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.root1.simon.codec.messages.AbstractMessage;
import de.root1.simon.codec.messages.MsgLookup;
import de.root1.simon.codec.messages.SimonMessageConstants;
import de.root1.simon.utils.Utils;
import java.nio.BufferUnderflowException;

/**
 * A {@link MessageDecoder} that decodes {@link MsgLookup}.
 *
 * @author ACHR
 */
public class MsgLookupDecoder extends AbstractMessageDecoder {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public MsgLookupDecoder() {
        super(SimonMessageConstants.MSG_LOOKUP);
    }

    @Override
    protected AbstractMessage decodeBody(IoSession session, IoBuffer in) {

        MsgLookup m = new MsgLookup();

        try {
            String remoteObjectName = in.getPrefixedString(Charset.forName("UTF-8").newDecoder());
            m.setRemoteObjectName(remoteObjectName);
        } catch (CharacterCodingException e) {
            e.printStackTrace();
        } catch (BufferUnderflowException e) {
            System.err.println("Buffer Under Flow Exception !!");
            e.printStackTrace();
            System.out.flush();
            System.err.flush();
            System.exit(0);
        }

        if (logger.isTraceEnabled())
            logger.trace("message={} on session={}", m, Utils.longToHexString(session.getId()));
        
        return m;
    }

    public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
    }
}
