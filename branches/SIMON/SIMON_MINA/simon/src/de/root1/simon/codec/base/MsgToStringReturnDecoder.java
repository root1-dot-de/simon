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
import de.root1.simon.codec.messages.MsgToStringReturn;
import de.root1.simon.codec.messages.SimonMessageConstants;

/**
 * A {@link MessageDecoder} that decodes {@link MsgToStringReturn}.
 *
 * @author ACHR
 */
public class MsgToStringReturnDecoder extends AbstractMessageDecoder {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
    public MsgToStringReturnDecoder() {
        super(SimonMessageConstants.MSG_TOSTRING_RETURN);
    }
    
    @Override
    protected AbstractMessage decodeBody(IoSession session, IoBuffer in) {

    	MsgToStringReturn message = new MsgToStringReturn();
    	
        try {
        	
        	String returnValue = in.getPrefixedString(Charset.forName("UTF-8").newDecoder());
        	message.setReturnValue(returnValue);
        	
		} catch (CharacterCodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		logger.trace("message={}", message);
        return message;
    }
    
    public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
    }
    
   
}
