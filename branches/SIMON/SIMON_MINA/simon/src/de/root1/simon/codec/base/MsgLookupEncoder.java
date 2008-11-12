package de.root1.simon.codec.base;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.demux.MessageEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.root1.simon.codec.messages.MsgLookup;
import de.root1.simon.codec.messages.SimonMessageConstants;

/**
 * A {@link MessageEncoder} that encodes {@link MsgLookup}.
 *
 * @author ACHR
 */
public class MsgLookupEncoder<T extends MsgLookup> extends AbstractMessageEncoder<T> {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
    public MsgLookupEncoder() {
        super(SimonMessageConstants.MSG_LOOKUP);
    }

    @Override
    protected void encodeBody(IoSession session, T message, IoBuffer out) {
    	logger.trace("message={}", message);
        try {
			out.putString(message.getRemoteObjectName(),Charset.forName("UTF-8").newEncoder());
		} catch (CharacterCodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public void dispose() throws Exception {
    }
}
