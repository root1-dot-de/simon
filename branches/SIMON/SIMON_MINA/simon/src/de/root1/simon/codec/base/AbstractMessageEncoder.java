package de.root1.simon.codec.base;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.demux.MessageEncoder;

import de.root1.simon.codec.messages.AbstractMessage;

/**
 * A {@link MessageEncoder} that encodes message header and forwards
 * the encoding of body to a subclass.
 *
 * @author ACHR
 */
public abstract class AbstractMessageEncoder<T extends AbstractMessage> implements MessageEncoder<T> {
    private final int msgType;

    protected AbstractMessageEncoder(int msgType) {
        this.msgType = msgType;
    }

    public void encode(IoSession session, T message, ProtocolEncoderOutput out) throws Exception {
        IoBuffer buf = IoBuffer.allocate(16);
        buf.setAutoExpand(true); // Enable auto-expand for easier encoding

        // Encode a header
        buf.putShort((short) msgType);
        buf.putInt(message.getSequence());

        // Encode a body
        encodeBody(session, message, buf);
        buf.flip();
        out.write(buf);
    }

    /**
     * Encodes the body of the message.
     * This method has to be implemented by the message encoder class that extends this class
     * @param session
     * @param message
     * @param out
     */
    protected abstract void encodeBody(IoSession session, T message, IoBuffer out);
}