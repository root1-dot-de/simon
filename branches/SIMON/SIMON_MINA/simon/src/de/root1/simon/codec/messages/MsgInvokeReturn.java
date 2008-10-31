package de.root1.simon.codec.messages;


/**
 * <code>INVOKE RETURN</code> message 
 *
 * @author ACHR
 */
public class MsgInvokeReturn extends AbstractMessage {
    private static final long serialVersionUID = 1L;

    private Object returnValue;

    public MsgInvokeReturn() {
    	super(SimonMessageConstants.MSG_INVOKE_RETURN);
    }

    public Object getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(Object returnValue) {
        this.returnValue = returnValue;
    }

    @Override
    public String toString() {
        // it is a good practice to create toString() method on message classes.
        return getSequence() + ":MsgInvokeReturn(" + returnValue + ')';
    }
}
