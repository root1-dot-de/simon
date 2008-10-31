package de.root1.simon.codec.messages;



/**
 * <code>ToString</code> message
 *
 * @author ACHR
 */
public class MsgHashCode extends AbstractMessage {
	
    private static final long serialVersionUID = 1L;

    String remoteObjectName;
    
    public MsgHashCode() {
    	super(SimonMessageConstants.MSG_HASHCODE);
    }

    public String getRemoteObjectName() {
        return remoteObjectName;
    }

    public void setRemoteObjectName(String remoteObjectName) {
        this.remoteObjectName = remoteObjectName;
    }

    @Override
    public String toString() {
        // it is a good practice to create toString() method on message classes.
        return getSequence() + ":MsgHashCode(ron=" + remoteObjectName + ")";
    }

}
