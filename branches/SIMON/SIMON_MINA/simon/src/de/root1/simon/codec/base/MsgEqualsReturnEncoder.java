package de.root1.simon.codec.base;
import java.util.logging.Logger;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.demux.MessageEncoder;

import de.root1.simon.codec.messages.MsgEqualsReturn;
import de.root1.simon.codec.messages.SimonMessageConstants;

/**
 * A {@link MessageEncoder} that encodes {@link MsgEqualsReturn}.
 *
 * @author ACHR
 */
public class MsgEqualsReturnEncoder<T extends MsgEqualsReturn> extends AbstractMessageEncoder<T> {
	
	protected transient Logger _log = Logger.getLogger(this.getClass().getName());
	
    public MsgEqualsReturnEncoder() {
        super(SimonMessageConstants.MSG_EQUALS_RETURN);
    }

    @Override
    protected void encodeBody(IoSession session, T message, IoBuffer out) {
    	
    	_log.finer("begin. message="+message);
    	// true  -> 0xFF
    	// false -> 0x00
    	out.put(message.getEqualsResult() == true ? (byte)0xFF : (byte)0x00);
		_log.finer("end");
    }

    public void dispose() throws Exception {
    }
}
