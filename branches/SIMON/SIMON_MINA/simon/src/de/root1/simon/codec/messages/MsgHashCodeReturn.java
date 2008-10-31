package de.root1.simon.codec.messages;


/**
 * <code>ToString RETURN</code> message 
 *
 * @author ACHR
 */
public class MsgHashCodeReturn extends AbstractMessage {
    private static final long serialVersionUID = 1L;

    private int returnValue;

    public MsgHashCodeReturn() {
    	super(SimonMessageConstants.MSG_HASHCODE_RETURN);
    }

    public int getReturnValue() {
    	return returnValue;
    }

    public void setReturnValue(int returnValue) {
        this.returnValue = returnValue;
    }

    @Override
    public String toString() {
        // it is a good practice to create toString() method on message classes.
        return getSequence() + ":MsgHashCodeReturn(" + returnValue + ')';
    }
}
