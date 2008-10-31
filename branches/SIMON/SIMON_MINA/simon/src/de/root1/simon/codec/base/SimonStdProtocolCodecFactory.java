package de.root1.simon.codec.base;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.demux.DemuxingProtocolCodecFactory;

import de.root1.simon.codec.messages.MsgInvoke;
import de.root1.simon.codec.messages.MsgInvokeReturn;
import de.root1.simon.codec.messages.MsgLookup;
import de.root1.simon.codec.messages.MsgLookupReturn;
import de.root1.simon.codec.messages.MsgToString;
import de.root1.simon.codec.messages.MsgToStringReturn;

/**
 * A {@link ProtocolCodecFactory} that provides a protocol codec for
 * Simon Standard protocol.
 *
 * @author ACHR
 */
public class SimonStdProtocolCodecFactory extends DemuxingProtocolCodecFactory {

    public SimonStdProtocolCodecFactory(boolean server) {
        
    	if (server) { // **** SERVER **** 
        	// incoming lookup
            super.addMessageDecoder(MsgLookupDecoder.class);
            // outgoing lookup return
            super.addMessageEncoder(MsgLookupReturn.class, MsgLookupReturnEncoder.class);
        } 
        else // **** CLIENT ****
        {
        	// outgoing lookup
        	super.addMessageEncoder(MsgLookup.class, MsgLookupEncoder.class);
        	// incoming lookup return
        	super.addMessageDecoder(MsgLookupReturnDecoder.class);        	
        }
        
        /* *****************************************
         *  Encoder and Decoder for both sides
         * *****************************************/
        
    	/*
    	 * invocation handling
    	 */
    	
    	// outgoing invoke 
    	super.addMessageEncoder(MsgInvoke.class, MsgInvokeEncoder.class);
    	// incoming invoke return 
    	super.addMessageDecoder(MsgInvokeReturnDecoder.class);

    	// incoming invoke
        super.addMessageDecoder(MsgInvokeDecoder.class);
        // outgoing invoke return
        super.addMessageEncoder(MsgInvokeReturn.class, MsgInvokeReturnEncoder.class);
        
        /*
         * "toString()" handling
         */
        
        // outgoing toString
        super.addMessageEncoder(MsgToString.class, MsgToStringEncoder.class);
        // incoming toString return
        super.addMessageDecoder(MsgToStringReturnDecoder.class);
        
        // incoming toString
        super.addMessageDecoder(MsgToStringDecoder.class);
        // outgoing toString return
        super.addMessageEncoder(MsgToStringReturn.class, MsgToStringReturnEncoder.class);
    }
}

