package de.root1.simon.codec.base;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.logging.Logger;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;

import de.root1.simon.codec.messages.AbstractMessage;
import de.root1.simon.codec.messages.MsgLookup;

/**
 * A {@link MessageDecoder} that decodes {@link MsgLookup}.
 *
 * @author ACHR
 */
public class MsgLookupDecoder extends AbstractMessageDecoder {
	
	protected transient Logger _log = Logger.getLogger(this.getClass().getName());

	
	private MsgLookup m = new MsgLookup();
	
    public MsgLookupDecoder() {
        super(SimonStdProtocolConstants.LOOKUP_MSG);
    }

    @Override
    protected AbstractMessage decodeBody(IoSession session, IoBuffer in) {

    	
        
        try {
        	String remoteObjectName = in.getString(Charset.forName("UTF-8").newDecoder());
			m.setRemoteObjectName(remoteObjectName);
		} catch (CharacterCodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		_log.finer("message="+m);
        return m;
    }

    public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
    }
}
