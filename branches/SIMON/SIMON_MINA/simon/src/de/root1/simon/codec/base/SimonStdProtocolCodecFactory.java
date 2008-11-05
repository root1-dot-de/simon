package de.root1.simon.codec.base;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.demux.DemuxingProtocolCodecFactory;

import de.root1.simon.codec.messages.MsgEquals;
import de.root1.simon.codec.messages.MsgEqualsReturn;
import de.root1.simon.codec.messages.MsgHashCode;
import de.root1.simon.codec.messages.MsgHashCodeReturn;
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
        
        /*
         * "hashCode()" handling
         */
        
        // outgoing hashCode
        super.addMessageEncoder(MsgHashCode.class, MsgHashCodeEncoder.class);
        // incoming hashCode return
        super.addMessageDecoder(MsgHashCodeReturnDecoder.class);
        
        // incoming hashCode
        super.addMessageDecoder(MsgHashCodeDecoder.class);
        // outgoing hashCode return
        super.addMessageEncoder(MsgHashCodeReturn.class, MsgHashCodeReturnEncoder.class);
        
        /*
         * "equals()" handling
         */
        
        // outgoing equals
        super.addMessageEncoder(MsgEquals.class, MsgEqualsEncoder.class);
        // incoming equals return
        super.addMessageDecoder(MsgEqualsReturnDecoder.class);
        
        // incoming equals
        super.addMessageDecoder(MsgEqualsDecoder.class);
        // outgoing equals return
        super.addMessageEncoder(MsgEqualsReturn.class, MsgEqualsReturnEncoder.class);
    }
}

