package de.root1.simon.codec.base;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.demux.MessageEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.root1.simon.codec.messages.MsgLookupReturn;
import de.root1.simon.codec.messages.SimonMessageConstants;

/**
 * A {@link MessageEncoder} that encodes {@link MsgLookupReturn}.
 *
 * @author ACHR
 */
public class MsgLookupReturnEncoder<T extends MsgLookupReturn> extends AbstractMessageEncoder<T> {
    
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public MsgLookupReturnEncoder() {
        super(SimonMessageConstants.MSG_LOOKUP_RETURN);
    }

    @Override
    protected void encodeBody(IoSession session, T message, IoBuffer out) {
    	logger.trace("sending interfaces ...");
    	Class<?>[] interfaces = message.getInterfaces();
    	out.putInt(interfaces.length);
    	for (Class<?> class1 : interfaces) {
			try {
				logger.trace("interface={}", class1.getCanonicalName());
				out.putPrefixedString(class1.getCanonicalName(), Charset.forName("UTF-8").newEncoder());
			} catch (CharacterCodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    	logger.trace("finished");
    }

    public void dispose() throws Exception {
    }
}
