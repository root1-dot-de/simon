package de.root1.simon.codec.base;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.demux.MessageEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.root1.simon.codec.messages.MsgInvoke;
import de.root1.simon.codec.messages.SimonMessageConstants;
import de.root1.simon.utils.Utils;

/**
 * A {@link MessageEncoder} that encodes {@link MsgInvoke}.
 *
 * @author ACHR
 */
public class MsgInvokeEncoder<T extends MsgInvoke> extends AbstractMessageEncoder<T> {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
    public MsgInvokeEncoder() {
        super(SimonMessageConstants.MSG_INVOKE);
    }

    @Override
    protected void encodeBody(IoSession session, T message, IoBuffer out) {
    	
    	logger.trace("begin. message={}", message);
        try {
        	out.putPrefixedString(message.getRemoteObjectName(),Charset.forName("UTF-8").newEncoder());
			out.putLong(Utils.computeMethodHash(message.getMethod()));
		
			int argsLen=0;
			
			if (message.getArguments()!=null) 
				argsLen = message.getArguments().length;
			
			logger.trace("argsLength={}", argsLen);
			out.putInt(argsLen);
			
			for (int i=0; i<argsLen;i++){
				logger.trace("args[{}]={}", i, message.getArguments()[i]);
				out.putObject(message.getArguments()[i]);
			}
			
		} catch (CharacterCodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.trace("end");
    }

    public void dispose() throws Exception {
    }
}
