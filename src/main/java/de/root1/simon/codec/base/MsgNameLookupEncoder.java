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

import de.root1.simon.codec.messages.MsgError;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.demux.MessageEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.root1.simon.codec.messages.MsgNameLookup;
import de.root1.simon.utils.Utils;

/**
 * A {@link MessageEncoder} that encodes {@link MsgNameLookup}.
 *
 * @author ACHR
 */
public class MsgNameLookupEncoder<T extends MsgNameLookup> extends AbstractMessageEncoder<T> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected void encodeBody(IoSession session, T message, IoBuffer out) {

        logger.trace("begin. message={}", message);
        logger.trace("position before: {}",out.position());
        try {
            out.putPrefixedString(message.getRemoteObjectName(), Charset.forName("UTF-8").newEncoder());
        } catch (CharacterCodingException e) {
            MsgError error = new MsgError();
            error.setEncodeError();
            error.setErrorMessage("Error while encoding name lookup() request: Not able to write remote object name '"+message.getRemoteObjectName()+"' due to CharacterCodingException.");
            error.setRemoteObjectName(null);
            error.setInitSequenceId(message.getSequence());
            error.setThrowable(e);
            sendEncodingError(out, session, error);
        }
        logger.trace("position after: {}",out.position());
        logger.trace("end");
    }

}
