package de.root1.simon.codec.base;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.logging.Logger;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.demux.MessageEncoder;

import de.root1.simon.codec.messages.MsgHashCode;
import de.root1.simon.codec.messages.SimonMessageConstants;

/**
 * A {@link MessageEncoder} that encodes {@link MsgHashCode}.
 *
 * @author ACHR
 */
public class MsgHashCodeEncoder<T extends MsgHashCode> extends AbstractMessageEncoder<T> {
	
	protected transient Logger _log = Logger.getLogger(this.getClass().getName());
	
    public MsgHashCodeEncoder() {
        super(SimonMessageConstants.MSG_HASHCODE);
    }

    @Override
    protected void encodeBody(IoSession session, T message, IoBuffer out) {
    	
    	_log.finer("begin. message="+message);
        try {
        	out.putPrefixedString(message.getRemoteObjectName(),Charset.forName("UTF-8").newEncoder());
		} catch (CharacterCodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		_log.finer("end");
    }

    public void dispose() throws Exception {
    }
}
