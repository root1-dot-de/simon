package de.root1.simon.codec.base;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.demux.MessageEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.root1.simon.codec.messages.MsgInvokeReturn;
import de.root1.simon.codec.messages.MsgLookup;
import de.root1.simon.codec.messages.SimonMessageConstants;

/**
 * A {@link MessageEncoder} that encodes {@link MsgLookup}.
 *
 * @author ACHR
 */
public class MsgInvokeReturnEncoder<T extends MsgInvokeReturn> extends AbstractMessageEncoder<T> {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

    public MsgInvokeReturnEncoder() {
        super(SimonMessageConstants.MSG_INVOKE_RETURN);
    }

    @Override
    protected void encodeBody(IoSession session, T message, IoBuffer out) {
    	logger.trace("begin. message={}", message);
    	out.putObject(message.getReturnValue());
    	logger.trace("end.");
    }

    public void dispose() throws Exception {
    }
}
