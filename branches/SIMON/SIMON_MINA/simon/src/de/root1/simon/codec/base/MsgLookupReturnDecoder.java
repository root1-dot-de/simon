package de.root1.simon.codec.base;
import java.util.logging.Logger;

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
	
	protected transient Logger _log = Logger.getLogger(this.getClass().getName());

    public MsgLookupReturnDecoder() {
        super(SimonStdProtocolConstants.LOOKUP_RETURN_MSG);
    }

    @Override
    protected AbstractMessage decodeBody(IoSession session, IoBuffer in) {
    	
    	_log.finer("decoding ...");
    	MsgLookupReturn m = new MsgLookupReturn();
    	
    	try {
    		_log.finer("trying to read interfaces value");
    		Class<?>[] interfaces = (Class<?>[])in.getObject();
    		_log.finer("got it ...");
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
