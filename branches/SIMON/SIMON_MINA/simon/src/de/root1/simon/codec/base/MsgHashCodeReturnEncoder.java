package de.root1.simon.codec.base;
import java.util.logging.Logger;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.demux.MessageEncoder;

import de.root1.simon.codec.messages.MsgHashCodeReturn;
import de.root1.simon.codec.messages.SimonMessageConstants;

/**
 * A {@link MessageEncoder} that encodes {@link MsgHashCodeReturn}.
 *
 * @author ACHR
 */
public class MsgHashCodeReturnEncoder<T extends MsgHashCodeReturn> extends AbstractMessageEncoder<T> {
	
	protected transient Logger _log = Logger.getLogger(this.getClass().getName());
	
    public MsgHashCodeReturnEncoder() {
        super(SimonMessageConstants.MSG_HASHCODE_RETURN);
    }

    @Override
    protected void encodeBody(IoSession session, T message, IoBuffer out) {
    	
    	_log.finer("begin. message="+message);
    	out.putInt(message.getReturnValue());
		_log.finer("end");
    }

    public void dispose() throws Exception {
    }
}
