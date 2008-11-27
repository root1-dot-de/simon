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
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.root1.simon.LookupTable;
import de.root1.simon.codec.messages.AbstractMessage;
import de.root1.simon.codec.messages.MsgInvoke;
import de.root1.simon.codec.messages.SimonMessageConstants;

/**
 * A {@link MessageDecoder} that decodes {@link MsgInvoke}.
 *
 * @author ACHR
 */
public class MsgInvokeDecoder extends AbstractMessageDecoder {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
//	private int msgSize;
//	private boolean readSize = false;
	
    public MsgInvokeDecoder() {
        super(SimonMessageConstants.MSG_INVOKE);
    }
    
    public class InvokeState{
    	public boolean readSize = false;
    	public int msgSize;
    }
    
    @Override
    protected AbstractMessage decodeBody(IoSession session, IoBuffer in) {
    	InvokeState is;
    	if (session.getAttribute("invoke_seq="+getCurrentSequence())==null) {
//    	if (!readSize) {
    		logger.trace("Reading size of msg for sequenceId={}...",getCurrentSequence());
    		is = new InvokeState();
    		is.readSize  = true;
    		is.msgSize = in.getInt();
    		logger.trace("seqId={}, msgSizeInBytes={} position={}",new Object[]{getCurrentSequence(), is.msgSize, in.position()});
    	} else {
    	
    		is = (InvokeState) session.getAttribute("invoke_seq="+getCurrentSequence());
    		
	    	if (in.remaining() < is.msgSize) {
	    		logger.trace("need more data for seqId={}, needed={} position={}, avail={}",new Object[]{getCurrentSequence(), is.msgSize, in.position(),in.remaining()});
	    		return null;
	    	}
    	}
    	
    	MsgInvoke msgInvoke = new MsgInvoke();
    	
        try {
        	LookupTable lookupTable = (LookupTable) session.getAttribute("LookupTable");
        
        	logger.trace("start pos={} capacity={}",in.position(), in.capacity());
        	String remoteObjectName = in.getPrefixedString(Charset.forName("UTF-8").newDecoder());
        	msgInvoke.setRemoteObjectName(remoteObjectName);
        	logger.trace("ron read ... pos={}",in.position());
        	

    		long methodHash = in.getLong();
    		Method method = lookupTable.getMethod(msgInvoke.getRemoteObjectName(), methodHash);
    		logger.trace("methodHash read ... pos={}",in.position());
	
		
    		
			int argsLength = in.getInt();
			logger.trace("args len read read ... pos={}",in.position());
			logger.trace("getting {} args", argsLength);
			Object[] args = new Object[argsLength];
			for (int i=0;i<argsLength;i++){
				args[i]=in.getObject();
				logger.trace("{} object read ... pos={} object={}", new Object[]{i, in.position(), args[i]});
			}
			
			msgInvoke.setArguments(args);
			msgInvoke.setRemoteObjectName(remoteObjectName);
			msgInvoke.setMethod(method);
		} catch (CharacterCodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		session.removeAttribute("invoke_seq="+getCurrentSequence());
		
		logger.trace("message={}", msgInvoke);
        return msgInvoke;
    }
    
    public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
    }
    
   
}
