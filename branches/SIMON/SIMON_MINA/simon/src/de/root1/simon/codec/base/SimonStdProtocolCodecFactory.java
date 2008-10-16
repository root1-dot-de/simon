package de.root1.simon.codec.base;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.demux.DemuxingProtocolCodecFactory;

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
        if (server) {
            super.addMessageDecoder(MsgLookupDecoder.class);
            super.addMessageEncoder(MsgLookupReturn.class, MsgLookupReturnEncoder.class);
        } else // Client
        {
            super.addMessageEncoder(MsgLookup.class, MsgLookupEncoder.class);
            super.addMessageDecoder(MsgLookupReturnDecoder.class);
        }
    }
}
