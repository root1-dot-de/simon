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
import de.root1.simon.codec.messages.MsgLookup;

/**
 * A {@link MessageDecoder} that decodes {@link MsgLookup}.
 *
 * @author ACHR
 */
public class MsgInvokeDecoder extends AbstractMessageDecoder {

    public MsgInvokeDecoder() {
        super(SimonStdProtocolConstants.INVOKE_MSG);
    }

    @Override
    protected AbstractMessage decodeBody(IoSession session, IoBuffer in) {

    	System.out.println("MsgInvokeDecoder#decodeBody(): ");
        MsgInvoke m = new MsgInvoke();
        try {
        	String remoteObjectName = in.getString(Charset.forName("UTF-8").newDecoder());
			m.setRemoteObjectName(remoteObjectName);
			LookupTable lookupTable = (LookupTable) session.getAttribute("LookupTable");
			Method method = lookupTable.getMethod(remoteObjectName, in.getLong());
			Object[] args = (Object[]) in.getObject();
			
			m.setArguments(args);
			m.setRemoteObjectName(remoteObjectName);
			m.setMethod(method);
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
        return m;
    }

    public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
    }
}
