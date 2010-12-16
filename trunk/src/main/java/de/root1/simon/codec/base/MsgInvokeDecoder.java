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

import java.lang.reflect.Method;
import java.nio.charset.Charset;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.root1.simon.LookupTable;
import de.root1.simon.Statics;
import de.root1.simon.codec.messages.AbstractMessage;
import de.root1.simon.codec.messages.MsgError;
import de.root1.simon.codec.messages.MsgInvoke;
import de.root1.simon.codec.messages.SimonMessageConstants;
import de.root1.simon.utils.Utils;
import java.nio.BufferUnderflowException;

/**
 * A {@link MessageDecoder} that decodes {@link MsgInvoke}.
 *
 * @author ACHR
 */
public class MsgInvokeDecoder extends AbstractMessageDecoder {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public MsgInvokeDecoder() {
        super(SimonMessageConstants.MSG_INVOKE);
    }

    @Override
    protected AbstractMessage decodeBody(IoSession session, IoBuffer in) {

        MsgInvoke msgInvoke = new MsgInvoke();
        String remoteObjectName=null;
        try {

            LookupTable lookupTable = (LookupTable) session.getAttribute(Statics.SESSION_ATTRIBUTE_LOOKUPTABLE);

            logger.trace("start pos={} capacity={}", in.position(), in.capacity());
            remoteObjectName = in.getPrefixedString(Charset.forName("UTF-8").newDecoder());
            msgInvoke.setRemoteObjectName(remoteObjectName);
            logger.trace("remote object name read ... remoteObjectName={} pos={}", remoteObjectName, in.position());

            long methodHash = in.getLong();
            logger.trace("got method hash {}", methodHash);
            Method method = lookupTable.getMethod(msgInvoke.getRemoteObjectName(), methodHash);
            logger.trace("method looked up ... pos={} method=[{}]", in.position(), method.toString());

            int argsLength = in.getInt();
            logger.trace("args len read read ... pos={}", in.position());
            logger.trace("getting {} args", argsLength);
            Object[] args = new Object[argsLength];
            for (int i = 0; i < argsLength; i++) {
                try {
                    args[i] = in.getObject();
                } catch (Exception ex) {
                    Exception ex1 = new Exception("Problem reading method argument. Maybe argument isn't serializable?!");
                    ex1.initCause(ex.getCause());
                    ex1.setStackTrace(ex.getStackTrace());
                    logger.error("Exception while reading arguments.", ex);
                    throw ex1;
                }
                logger.trace("arg #{} read ... pos={} object={}", new Object[]{i, in.position(), args[i]});
            }

            msgInvoke.setArguments(args);
            msgInvoke.setRemoteObjectName(remoteObjectName);
            msgInvoke.setMethod(method);

        } catch (Exception e) {
            MsgError error = new MsgError();
            error.setErrorMessage("Error while decoding invoke request");
            error.setRemoteObjectName(remoteObjectName);
            error.setThrowable(e);
            return error;
        } 

        logger.trace("message={}", msgInvoke);
        return msgInvoke;
    }

    @Override
    public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
    }
}
