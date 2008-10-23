package de.root1.simon.codec.messages;
import java.io.Serializable;

/**
 * A base message for SumUp protocol messages.
 *
 * @author The Apache MINA Project (dev@mina.apache.org)
 * @version $Rev: 576217 $, $Date: 2007-09-17 01:55:27 +0200 (lun, 17 sep 2007) $
 */
public abstract class AbstractMessage implements Serializable {
    
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int sequence = -1;

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }
}