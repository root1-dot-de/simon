package de.root1.simon.codec.messages;



/**
 * <code>Equals</code> message
 *
 * @author ACHR
 */
public class MsgEquals extends AbstractMessage {
	
    private static final long serialVersionUID = 1L;

    String remoteObjectName;
    Object objectToCompareWith;
    
	public MsgEquals() {
    	super(SimonMessageConstants.MSG_EQUALS);
    }

    public String getRemoteObjectName() {
        return remoteObjectName;
    }

    public void setRemoteObjectName(String remoteObjectName) {
        this.remoteObjectName = remoteObjectName;
    }
    
	public Object getObjectToCompareWith() {
		return objectToCompareWith;
	}

	public void setObjectToCompareWith(Object objectToCompareWith) {
		this.objectToCompareWith = objectToCompareWith;
	}

    @Override
    public String toString() {
        // it is a good practice to create toString() method on message classes.
        return getSequence() + ":MsgEquals(ron=" + remoteObjectName + "|objectToCompareWith="+objectToCompareWith+")";
    }

}
