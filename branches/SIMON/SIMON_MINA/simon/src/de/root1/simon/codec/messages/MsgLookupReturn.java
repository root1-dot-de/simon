package de.root1.simon.codec.messages;
/**
 * Lookup return message
 *
 * @author ACHR
 */
public class MsgLookupReturn extends AbstractMessage {
	
    private static final long serialVersionUID = 1L;

    private Class<?>[] interfaces;

    public MsgLookupReturn() {
    }

    public Class<?>[] getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(Class<?>[] interfaces) {
        this.interfaces = interfaces;
    }

    @Override
    public String toString() {
        return getSequence() + ":MsgLookupReturn(" + interfaces + ')';
    }
}