package de.root1.simon.codec.base;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;

import de.root1.simon.codec.messages.AbstractMessage;
import de.root1.simon.codec.messages.MsgLookupReturn;

/**
 * A {@link MessageDecoder} that decodes {@link MsgLookupReturn}.
 *
 * @author ACHR
 */
public class MsgLookupReturnDecoder extends AbstractMessageDecoder {

    public MsgLookupReturnDecoder() {
        super(SimonStdProtocolConstants.LOOKUP_RETURN_MSG);
    }

    @Override
    protected AbstractMessage decodeBody(IoSession session, IoBuffer in) {
    	
    	MsgLookupReturn m = new MsgLookupReturn();
    	
    	try {
    		Class<?>[] interfaces = (Class<?>[])in.getObject();
			m.setInterfaces( interfaces);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return m;
    }

    public void finishDecode(IoSession session, ProtocolDecoderOutput out)
            throws Exception {
    }
}
