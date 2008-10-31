package de.root1.simon.codec.messages;



/**
 * <code>ToString</code> message
 *
 * @author ACHR
 */
public class MsgToString extends AbstractMessage {
	
    private static final long serialVersionUID = 1L;

    String remoteObjectName;
    
    public MsgToString() {
    	super(SimonMessageConstants.MSG_TOSTRING);
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
        return getSequence() + ":MsgToString(ron=" + remoteObjectName + ")";
    }

}
