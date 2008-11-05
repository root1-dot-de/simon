package de.root1.simon.codec.base;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.logging.Logger;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;

import de.root1.simon.codec.messages.AbstractMessage;
import de.root1.simon.codec.messages.MsgEquals;
import de.root1.simon.codec.messages.SimonMessageConstants;

/**
 * A {@link MessageDecoder} that decodes {@link MsgEquals}.
 *
 * @author ACHR
 */
public class MsgEqualsDecoder extends AbstractMessageDecoder {
	
	protected transient Logger _log = Logger.getLogger(this.getClass().getName());
	
    public MsgEqualsDecoder() {
        super(SimonMessageConstants.MSG_EQUALS);
    }
    
    @Override
    protected AbstractMessage decodeBody(IoSession session, IoBuffer in) {

    	MsgEquals message = new MsgEquals();
    	
        try {
        	
	        	String remoteObjectName = in.getPrefixedString(Charset.forName("UTF-8").newDecoder());
	        	Object objectToCompareWith = in.getObject();
	        	message.setRemoteObjectName(remoteObjectName);
	        	message.setObjectToCompareWith(objectToCompareWith);
		} catch (CharacterCodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		_log.finer("message="+message);
        return message;
    }
    
    public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
    }
    
   
}
