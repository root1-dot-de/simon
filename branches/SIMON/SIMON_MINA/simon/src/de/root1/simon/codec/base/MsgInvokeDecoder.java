package de.root1.simon.codec.base;
import java.lang.reflect.Method;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

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

	
    public MsgInvokeDecoder() {
        super(SimonStdProtocolConstants.INVOKE_MSG);
    }
    
    private static class DecoderState {
    	
    }

    @Override
    protected AbstractMessage decodeBody(IoSession session, IoBuffer in) {

    	MsgInvoke msgInvoke = new MsgInvoke();
    	
    	System.out.println("MsgInvokeDecoder#decodeBody(): ");
        try {
        	
	        	String remoteObjectName = in.getPrefixedString(Charset.forName("UTF-8").newDecoder());
	        	
	        	msgInvoke.setRemoteObjectName(remoteObjectName);
        		// ---------- Get Long (8 bytes)
	        		LookupTable lookupTable = (LookupTable) session.getAttribute("LookupTable");
	        		Method method = lookupTable.getMethod(msgInvoke.getRemoteObjectName(), in.getLong());
			
				
				
				Object[] args = (Object[]) in.getObject();
			
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
        return msgInvoke;
    }
    
    public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
    }
    
   
}
