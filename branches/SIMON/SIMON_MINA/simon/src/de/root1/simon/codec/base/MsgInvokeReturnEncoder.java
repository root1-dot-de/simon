package de.root1.simon.codec.base;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.demux.MessageEncoder;

import de.root1.simon.codec.messages.MsgInvokeReturn;
import de.root1.simon.codec.messages.MsgLookup;

/**
 * A {@link MessageEncoder} that encodes {@link MsgLookup}.
 *
 * @author ACHR
 */
public class MsgInvokeReturnEncoder<T extends MsgInvokeReturn> extends AbstractMessageEncoder<T> {
	
    public MsgInvokeReturnEncoder() {
        super(SimonStdProtocolConstants.LOOKUP_MSG);
    }

    @Override
    protected void encodeBody(IoSession session, T message, IoBuffer out) {
    	System.out.println("MsgLookupEncoder#encodeBody(): message="+message);
//        try {
//			out.putString(message.getRemoteObjectName(),Charset.forName("UTF-8").newEncoder());
//		} catch (CharacterCodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
    }

    public void dispose() throws Exception {
    }
}
