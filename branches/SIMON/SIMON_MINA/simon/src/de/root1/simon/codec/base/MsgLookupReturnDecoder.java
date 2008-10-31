package de.root1.simon.codec.base;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.logging.Logger;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;

import de.root1.simon.codec.messages.AbstractMessage;
import de.root1.simon.codec.messages.MsgLookupReturn;
import de.root1.simon.codec.messages.SimonMessageConstants;

/**
 * A {@link MessageDecoder} that decodes {@link MsgLookupReturn}.
 *
 * @author ACHR
 */
public class MsgLookupReturnDecoder extends AbstractMessageDecoder {
	
	protected transient Logger _log = Logger.getLogger(this.getClass().getName());

    public MsgLookupReturnDecoder() {
        super(SimonMessageConstants.MSG_LOOKUP_RETURN);
    }

    @Override
    protected AbstractMessage decodeBody(IoSession session, IoBuffer in) {
    	
    	_log.finer("decoding ...");
    	MsgLookupReturn m = new MsgLookupReturn();
    	
    	try {
    		_log.finer("trying to read interfaces value");
    		int arraySize = in.getInt();
    		Class<?>[] interfaces = new Class<?>[arraySize];
    		for (int i=0;i<arraySize;i++){
    			interfaces[i] = Class.forName(in.getPrefixedString(Charset.forName("UTF-8").newDecoder()));
    			_log.finer("got interface="+interfaces[i].getCanonicalName());
    		}
    		
//    		Class<?>[] interfaces = (Class<?>[])in.getObject();
    		_log.finer("finished");
			m.setInterfaces( interfaces);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CharacterCodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return m;
    }

    public void finishDecode(IoSession session, ProtocolDecoderOutput out)
            throws Exception {
    }
}
