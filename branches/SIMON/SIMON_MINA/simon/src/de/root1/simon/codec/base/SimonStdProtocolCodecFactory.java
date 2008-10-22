package de.root1.simon.codec.base;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.demux.DemuxingProtocolCodecFactory;

import de.root1.simon.codec.messages.MsgInvoke;
import de.root1.simon.codec.messages.MsgInvokeReturn;
import de.root1.simon.codec.messages.MsgLookup;
import de.root1.simon.codec.messages.MsgLookupReturn;

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
            
            // incoming invoke
            super.addMessageDecoder(MsgInvokeDecoder.class);
            // outgoing invoke return
            super.addMessageEncoder(MsgInvokeReturn.class, MsgInvokeReturnEncoder.class);
            
        } else // **** CLIENT ****
        {
        	// outgoing lookup
        	super.addMessageEncoder(MsgLookup.class, MsgLookupEncoder.class);
        	// incoming lookup return
        	super.addMessageDecoder(MsgLookupReturnDecoder.class);
        	
        	// outgoing invoke
        	super.addMessageEncoder(MsgInvoke.class, MsgInvokeEncoder.class);
        	// incoming invoke return
        	super.addMessageDecoder(MsgInvokeReturnDecoder.class);
        	
        }
    }
}
