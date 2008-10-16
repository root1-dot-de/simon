package de.root1.simon.codec.messages;
/**
 * <code>ADD</code> message in SumUp protocol.
 *
 * @author The Apache MINA Project (dev@mina.apache.org)
 * @version $Rev: 576217 $, $Date: 2007-09-17 01:55:27 +0200 (lun, 17 sep 2007) $
 */
public class MsgLookup extends AbstractMessage {
    private static final long serialVersionUID = -940833727168119141L;

    private String remoteObjectName;

    public MsgLookup() {
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
