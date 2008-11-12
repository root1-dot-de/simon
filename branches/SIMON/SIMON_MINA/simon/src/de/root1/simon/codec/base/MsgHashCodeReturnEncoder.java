package de.root1.simon.codec.base;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.demux.MessageEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.root1.simon.codec.messages.MsgHashCodeReturn;
import de.root1.simon.codec.messages.SimonMessageConstants;

/**
 * A {@link MessageEncoder} that encodes {@link MsgHashCodeReturn}.
 *
 * @author ACHR
 */
public class MsgHashCodeReturnEncoder<T extends MsgHashCodeReturn> extends AbstractMessageEncoder<T> {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
    public MsgHashCodeReturnEncoder() {
        super(SimonMessageConstants.MSG_HASHCODE_RETURN);
    }

    @Override
    protected void encodeBody(IoSession session, T message, IoBuffer out) {
    	
    	logger.trace("begin. message={}", message);
    	out.putInt(message.getReturnValue());
		logger.trace("end");
    }

    public void dispose() throws Exception {
    }
}
