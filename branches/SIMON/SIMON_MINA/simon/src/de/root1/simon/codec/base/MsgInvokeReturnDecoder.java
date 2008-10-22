package de.root1.simon.codec.base;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;

import de.root1.simon.codec.messages.AbstractMessage;
import de.root1.simon.codec.messages.MsgInvokeReturn;
import de.root1.simon.codec.messages.MsgLookup;

/**
 * A {@link MessageDecoder} that decodes {@link MsgLookup}.
 *
 * @author ACHR
 */
public class MsgInvokeReturnDecoder extends AbstractMessageDecoder {

    public MsgInvokeReturnDecoder() {
        super(SimonStdProtocolConstants.INVOKE_RETURN_MSG);
    }

    @Override
    protected AbstractMessage decodeBody(IoSession session, IoBuffer in) {

    	System.out.println("MsgLookupDecoder#decodeBody(): ");
        MsgInvokeReturn m = new MsgInvokeReturn();
    	try {
			Object returnValue = in.getObject();
			m.setReturnValue(returnValue);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return m;
    }

    public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
    }
}
