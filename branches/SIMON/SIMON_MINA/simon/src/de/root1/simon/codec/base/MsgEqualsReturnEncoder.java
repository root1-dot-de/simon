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
import org.apache.mina.filter.codec.demux.MessageEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.root1.simon.codec.messages.MsgEqualsReturn;
import de.root1.simon.codec.messages.SimonMessageConstants;

/**
 * A {@link MessageEncoder} that encodes {@link MsgEqualsReturn}.
 *
 * @author ACHR
 */
public class MsgEqualsReturnEncoder<T extends MsgEqualsReturn> extends AbstractMessageEncoder<T> {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
    public MsgEqualsReturnEncoder() {
        super(SimonMessageConstants.MSG_EQUALS_RETURN);
    }

    @Override
    protected void encodeBody(IoSession session, T message, IoBuffer out) {
    	
    	logger.trace("begin. message={}", message);
    	// true  -> 0xFF
    	// false -> 0x00
    	out.put(message.getEqualsResult() == true ? (byte)0xFF : (byte)0x00);
		logger.trace("end");
    }

    public void dispose() throws Exception {
    }
}
