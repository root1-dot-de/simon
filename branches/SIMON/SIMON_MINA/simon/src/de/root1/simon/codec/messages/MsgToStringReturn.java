package de.root1.simon.codec.messages;


/**
 * <code>ToString RETURN</code> message 
 *
 * @author ACHR
 */
public class MsgToStringReturn extends AbstractMessage {
    private static final long serialVersionUID = 1L;

    private String returnValue;

    public MsgToStringReturn() {
    	super(SimonMessageConstants.MSG_TOSTRING_RETURN);
    }

    public String getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(String returnValue) {
        this.returnValue = returnValue;
    }

    @Override
    public String toString() {
        // it is a good practice to create toString() method on message classes.
        return getSequence() + ":MsgToStringReturn(" + returnValue + ')';
    }
}
