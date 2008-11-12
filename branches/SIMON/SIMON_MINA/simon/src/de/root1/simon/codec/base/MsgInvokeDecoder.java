package de.root1.simon.codec.base;
import java.lang.reflect.Method;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.root1.simon.LookupTable;
import de.root1.simon.codec.messages.AbstractMessage;
import de.root1.simon.codec.messages.MsgInvoke;
import de.root1.simon.codec.messages.SimonMessageConstants;

/**
 * A {@link MessageDecoder} that decodes {@link MsgInvoke}.
 *
 * @author ACHR
 */
public class MsgInvokeDecoder extends AbstractMessageDecoder {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
    public MsgInvokeDecoder() {
        super(SimonMessageConstants.MSG_INVOKE);
    }
    
    @Override
    protected AbstractMessage decodeBody(IoSession session, IoBuffer in) {

    	MsgInvoke msgInvoke = new MsgInvoke();
    	
        try {
        	
        	String remoteObjectName = in.getPrefixedString(Charset.forName("UTF-8").newDecoder());
        	
        	msgInvoke.setRemoteObjectName(remoteObjectName);
    		LookupTable lookupTable = (LookupTable) session.getAttribute("LookupTable");
    		Method method = lookupTable.getMethod(msgInvoke.getRemoteObjectName(), in.getLong());
	
		
			int argsLength = in.getInt();
			logger.trace("getting {} args", argsLength);
			Object[] args = new Object[argsLength];
			for (int i=0;i<argsLength;i++){
				args[i]=in.getObject();
			}
			
			msgInvoke.setArguments(args);
			msgInvoke.setRemoteObjectName(remoteObjectName);
			msgInvoke.setMethod(method);
		} catch (CharacterCodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.trace("message={}", msgInvoke);
        return msgInvoke;
    }
    
    public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
    }
    
   
}
