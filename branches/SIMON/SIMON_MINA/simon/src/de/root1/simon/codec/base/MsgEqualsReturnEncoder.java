package de.root1.simon.codec.base;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.demux.MessageEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.root1.simon.codec.messages.MsgEqualsReturn;
import de.root1.simon.codec.messages.SimonMessageConstants;

/**
 * A {@link MessageEncoder} that encodes {@link MsgEqualsReturn}.
 *
 * @author ACHR
 */
public class MsgEqualsReturnEncoder<T extends MsgEqualsReturn> extends AbstractMessageEncoder<T> {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
    public MsgEqualsReturnEncoder() {
        super(SimonMessageConstants.MSG_EQUALS_RETURN);
    }

    @Override
    protected void encodeBody(IoSession session, T message, IoBuffer out) {
    	
    	logger.trace("begin. message={}", message);
    	// true  -> 0xFF
    	// false -> 0x00
    	out.put(message.getEqualsResult() == true ? (byte)0xFF : (byte)0x00);
		logger.trace("end");
    }

    public void dispose() throws Exception {
    }
}
