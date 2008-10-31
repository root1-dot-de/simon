package de.root1.simon.codec.messages;


/**
 * <code>LOOKUP</code> message
 *
 * @author ACHR
 */
public class MsgLookup extends AbstractMessage {
    private static final long serialVersionUID = 1L;

    private String remoteObjectName;

    public MsgLookup() {
    	super(SimonMessageConstants.MSG_LOOKUP);
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
        return getSequence() + ":MsgLookup(" + remoteObjectName + ')';
    }
}
