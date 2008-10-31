package de.root1.simon.codec.base;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.logging.Logger;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;

import de.root1.simon.codec.messages.AbstractMessage;
import de.root1.simon.codec.messages.MsgToString;
import de.root1.simon.codec.messages.SimonMessageConstants;

/**
 * A {@link MessageDecoder} that decodes {@link MsgToString}.
 *
 * @author ACHR
 */
public class MsgToStringDecoder extends AbstractMessageDecoder {
	
	protected transient Logger _log = Logger.getLogger(this.getClass().getName());
	
    public MsgToStringDecoder() {
        super(SimonMessageConstants.MSG_TOSTRING);
    }
    
    @Override
    protected AbstractMessage decodeBody(IoSession session, IoBuffer in) {

    	MsgToString message = new MsgToString();
    	
        try {
        	
	        	String remoteObjectName = in.getPrefixedString(Charset.forName("UTF-8").newDecoder());
	        	
	        	message.setRemoteObjectName(remoteObjectName);
		} catch (CharacterCodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		_log.finer("message="+message);
        return message;
    }
    
    public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
    }
    
   
}
