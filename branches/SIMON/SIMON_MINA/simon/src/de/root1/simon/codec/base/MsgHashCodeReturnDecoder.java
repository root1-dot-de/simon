package de.root1.simon.codec.base;
import java.util.logging.Logger;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;

import de.root1.simon.codec.messages.AbstractMessage;
import de.root1.simon.codec.messages.MsgHashCodeReturn;
import de.root1.simon.codec.messages.SimonMessageConstants;

/**
 * A {@link MessageDecoder} that decodes {@link MsgHashCodeReturn}.
 *
 * @author ACHR
 */
public class MsgHashCodeReturnDecoder extends AbstractMessageDecoder {
	
	protected transient Logger _log = Logger.getLogger(this.getClass().getName());
	
    public MsgHashCodeReturnDecoder() {
        super(SimonMessageConstants.MSG_HASHCODE_RETURN);
    }
    
    @Override
    protected AbstractMessage decodeBody(IoSession session, IoBuffer in) {
    	MsgHashCodeReturn message = new MsgHashCodeReturn();
    	int returnValue = in.getInt();
    	message.setReturnValue(returnValue);
		_log.finer("message="+message);
        return message;
    }
    
    public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
    }
    
   
}
