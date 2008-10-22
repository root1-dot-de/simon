package de.root1.simon.codec.base;
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
        super(SimonStdProtocolConstants.INVOKE_RETURN_MSG);
    }

    @Override
    protected void encodeBody(IoSession session, T message, IoBuffer out) {
    	System.out.println("MsgLookupEncoder#encodeBody(): message="+message);
    	out.putObject(message.getReturnValue());
    }

    public void dispose() throws Exception {
    }
}
