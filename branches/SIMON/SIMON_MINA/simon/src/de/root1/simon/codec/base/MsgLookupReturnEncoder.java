package de.root1.simon.codec.base;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.logging.Logger;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.demux.MessageEncoder;

import de.root1.simon.codec.messages.MsgLookupReturn;
import de.root1.simon.codec.messages.SimonMessageConstants;

/**
 * A {@link MessageEncoder} that encodes {@link MsgLookupReturn}.
 *
 * @author ACHR
 */
public class MsgLookupReturnEncoder<T extends MsgLookupReturn> extends AbstractMessageEncoder<T> {
    
	protected transient Logger _log = Logger.getLogger(this.getClass().getName());
	
	public MsgLookupReturnEncoder() {
        super(SimonMessageConstants.MSG_LOOKUP_RETURN);
    }

    @Override
    protected void encodeBody(IoSession session, T message, IoBuffer out) {
    	_log.finer("sending interfaces ...");
    	Class<?>[] interfaces = message.getInterfaces();
    	out.putInt(interfaces.length);
    	for (Class<?> class1 : interfaces) {
			try {
				_log.finer("interface="+class1.getCanonicalName());
				out.putPrefixedString(class1.getCanonicalName(), Charset.forName("UTF-8").newEncoder());
			} catch (CharacterCodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    	_log.finer("finished");
    }

    public void dispose() throws Exception {
    }
}
