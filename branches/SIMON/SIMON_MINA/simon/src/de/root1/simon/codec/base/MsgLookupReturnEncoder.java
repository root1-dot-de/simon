package de.root1.simon.codec.base;
import java.util.logging.Logger;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.demux.MessageEncoder;

import de.root1.simon.codec.messages.MsgLookupReturn;

/**
 * A {@link MessageEncoder} that encodes {@link MsgLookupReturn}.
 *
 * @author ACHR
 */
public class MsgLookupReturnEncoder<T extends MsgLookupReturn> extends AbstractMessageEncoder<T> {
    
	protected transient Logger _log = Logger.getLogger(this.getClass().getName());
	
	public MsgLookupReturnEncoder() {
        super(SimonStdProtocolConstants.LOOKUP_RETURN_MSG);
    }

    @Override
    protected void encodeBody(IoSession session, T message, IoBuffer out) {
    	_log.finer("message="+message+" interfacesvalue="+message.getInterfaces());
        out.putObject(message.getInterfaces());
    }

    public void dispose() throws Exception {
    }
}
