package de.root1.simon.codec.base;
import java.util.logging.Logger;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;

import de.root1.simon.codec.messages.AbstractMessage;

/**
 * A {@link MessageDecoder} that decodes message header and forwards
 * the decoding of body to a subclass.
 *
 * @author The Apache MINA Project (dev@mina.apache.org)
 * @version $Rev: 671827 $, $Date: 2008-06-26 10:49:48 +0200 (jeu, 26 jun 2008) $
 */
public abstract class AbstractMessageDecoder implements MessageDecoder {
	
	protected transient Logger _log = Logger.getLogger(this.getClass().getName());
	
    private final int msgType;

    private int sequence;

    private boolean readHeader;

    /**
     * Creates a new message decoder
     * @param msgType specifies a unique ID for the type of message
     */
    protected AbstractMessageDecoder(int msgType) {
        this.msgType = msgType;
    }

    public MessageDecoderResult decodable(IoSession session, IoBuffer in) {
        // Return NEED_DATA if the whole header is not read yet.
        if (in.remaining() < SimonStdProtocolConstants.HEADER_LEN) {
            return MessageDecoderResult.NEED_DATA;
        }

        // Return OK if type and bodyLength matches.
        if (msgType == in.getShort()) {
            return MessageDecoderResult.OK;
        }

        // Return NOT_OK if not matches.
        return MessageDecoderResult.NOT_OK;
    }

    public MessageDecoderResult decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
    	
        // Try to skip header if not read.
        if (!readHeader) {
            in.getShort(); // Skip 'type'.
            sequence = in.getInt(); // Get 'sequence'.
            readHeader = true;
        }

        // Try to decode body
        AbstractMessage m = decodeBody(session, in);
        // Return NEED_DATA if the body is not fully read.
        if (m == null) {
            return MessageDecoderResult.NEED_DATA;
        } else {
            readHeader = false; // reset readHeader for the next decode
        }
        m.setSequence(sequence);
        out.write(m);

        return MessageDecoderResult.OK;
    }

    /**
     * @return <tt>null</tt> if the whole body is not read yet
     */
    protected abstract AbstractMessage decodeBody(IoSession session, IoBuffer in);
}
