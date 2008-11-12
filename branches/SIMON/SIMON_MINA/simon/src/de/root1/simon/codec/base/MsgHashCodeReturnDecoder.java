package de.root1.simon.codec.base;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.root1.simon.codec.messages.AbstractMessage;
import de.root1.simon.codec.messages.MsgHashCodeReturn;
import de.root1.simon.codec.messages.SimonMessageConstants;

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
    	int returnValue = in.getInt();
    	message.setReturnValue(returnValue);
		logger.trace("message={}", message);
        return message;
    }
    
    public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
    }
    
   
}
