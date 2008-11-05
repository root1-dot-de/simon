package de.root1.simon.codec.base;
import java.util.logging.Logger;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;

import de.root1.simon.codec.messages.AbstractMessage;
import de.root1.simon.codec.messages.MsgEqualsReturn;
import de.root1.simon.codec.messages.SimonMessageConstants;

/**
 * A {@link MessageDecoder} that decodes {@link MsgEqualsReturn}.
 *
 * @author ACHR
 */
public class MsgEqualsReturnDecoder extends AbstractMessageDecoder {
	
	protected transient Logger _log = Logger.getLogger(this.getClass().getName());
	
    public MsgEqualsReturnDecoder() {
        super(SimonMessageConstants.MSG_EQUALS_RETURN);
    }
    
    @Override
    protected AbstractMessage decodeBody(IoSession session, IoBuffer in) {
    	MsgEqualsReturn message = new MsgEqualsReturn();
    	
    	// true  -> 0xFF
    	// false -> 0x00
    	
    	message.setEqualsResult(in.get() == 0xFF ? true : false);
    	
		_log.finer("message="+message);
        return message;
    }
    
    public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
    }
    
   
}
