package de.root1.simon.codec.base;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.root1.simon.codec.messages.AbstractMessage;
import de.root1.simon.codec.messages.MsgLookupReturn;
import de.root1.simon.codec.messages.SimonMessageConstants;

/**
 * A {@link MessageDecoder} that decodes {@link MsgLookupReturn}.
 *
 * @author ACHR
 */
public class MsgLookupReturnDecoder extends AbstractMessageDecoder {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
    public MsgLookupReturnDecoder() {
        super(SimonMessageConstants.MSG_LOOKUP_RETURN);
    }

    @Override
    protected AbstractMessage decodeBody(IoSession session, IoBuffer in) {
    	
    	logger.trace("decoding ...");
    	MsgLookupReturn m = new MsgLookupReturn();
    	
    	try {
    		logger.trace("trying to read interfaces value");
    		int arraySize = in.getInt();
    		Class<?>[] interfaces = new Class<?>[arraySize];
    		for (int i=0;i<arraySize;i++){
    			interfaces[i] = Class.forName(in.getPrefixedString(Charset.forName("UTF-8").newDecoder()));
    			logger.trace("got interface={}", interfaces[i].getCanonicalName());
    		}
    		
//    		Class<?>[] interfaces = (Class<?>[])in.getObject();
			m.setInterfaces( interfaces);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CharacterCodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
		logger.trace("finished");
    	return m;
    }

    public void finishDecode(IoSession session, ProtocolDecoderOutput out)
            throws Exception {
    }
}
