package de.root1.simon.codec.messages;


/**
 * <code>Equals RETURN</code> message 
 *
 * @author ACHR
 */
public class MsgEqualsReturn extends AbstractMessage {
    private static final long serialVersionUID = 1L;

    private boolean equalsResult;

    public MsgEqualsReturn() {
    	super(SimonMessageConstants.MSG_EQUALS_RETURN);
    }

    public boolean getEqualsResult() {
		return equalsResult;
	}

	public void setEqualsResult(boolean equalsResult) {
		this.equalsResult = equalsResult;
	}

	@Override
    public String toString() {
        // it is a good practice to create toString() method on message classes.
        return getSequence() + ":MsgEqualsReturn(" + equalsResult + ')';
    }
}
