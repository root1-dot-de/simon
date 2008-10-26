package de.root1.simon.codec.base;
import java.lang.reflect.Method;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.logging.Logger;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;

import de.root1.simon.LookupTable;
import de.root1.simon.codec.messages.AbstractMessage;
import de.root1.simon.codec.messages.MsgInvoke;

/**
 * A {@link MessageDecoder} that decodes {@link MsgInvoke}.
 *
 * @author ACHR
 */
public class MsgInvokeDecoder extends AbstractMessageDecoder {
	
	protected transient Logger _log = Logger.getLogger(this.getClass().getName());
	
    public MsgInvokeDecoder() {
        super(SimonStdProtocolConstants.INVOKE_MSG);
    }
    
    private static class DecoderState {
    	
    }

    @Override
    protected AbstractMessage decodeBody(IoSession session, IoBuffer in) {

    	MsgInvoke msgInvoke = new MsgInvoke();
    	
        try {
        	
	        	String remoteObjectName = in.getPrefixedString(Charset.forName("UTF-8").newDecoder());
	        	
	        	msgInvoke.setRemoteObjectName(remoteObjectName);
        		// ---------- Get Long (8 bytes)
	        		LookupTable lookupTable = (LookupTable) session.getAttribute("LookupTable");
	        		Method method = lookupTable.getMethod(msgInvoke.getRemoteObjectName(), in.getLong());
			
				
	    			int argsLength = in.getInt();
	    			_log.fine("getting "+argsLength+" args");
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
		_log.finer("message="+msgInvoke);
        return msgInvoke;
    }
    
    public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
    }
    
   
}
