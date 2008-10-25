package de.root1.simon.codec.base;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.demux.MessageEncoder;

import de.root1.simon.codec.messages.MsgInvoke;
import de.root1.simon.utils.Utils;

/**
 * A {@link MessageEncoder} that encodes {@link MsgInvoke}.
 *
 * @author ACHR
 */
public class MsgInvokeEncoder<T extends MsgInvoke> extends AbstractMessageEncoder<T> {
	
    public MsgInvokeEncoder() {
        super(SimonStdProtocolConstants.INVOKE_MSG);
    }

    @Override
    protected void encodeBody(IoSession session, T message, IoBuffer out) {
    	System.out.println("MsgInvokeEncoder#encodeBody(): message="+message);
        try {
        	out.putPrefixedString(message.getRemoteObjectName(),Charset.forName("UTF-8").newEncoder());
			out.putLong(Utils.computeMethodHash(message.getMethod()));
			out.putObject(message.getArguments());
		} catch (CharacterCodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public void dispose() throws Exception {
    }
}
