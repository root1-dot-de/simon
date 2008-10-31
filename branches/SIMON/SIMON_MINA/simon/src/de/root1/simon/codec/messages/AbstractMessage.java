package de.root1.simon.codec.messages;
import java.io.Serializable;

/**
 * A base message for SIMON protocol messages.
 *
 * @author ACHR
 */
public abstract class AbstractMessage implements Serializable {
    
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int msgType = -1;
	private int sequence = -1;
	
    /**
     * Creates a new message decoder
     * @param msgType specifies a unique ID for the type of message
     */
    protected AbstractMessage(int msgType) {
        this.msgType = msgType;
    }
	
	/**
	 * TODO
	 * @return the msgType
	 */
	public int getMsgType() {
		return msgType;
	}

	/**
	 * TODO
	 * @return the sequence
	 */
	public int getSequence() {
		return sequence;
	}
	
	/**
	 * TODO
	 * @param sequence the sequence to set
	 */
	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

}