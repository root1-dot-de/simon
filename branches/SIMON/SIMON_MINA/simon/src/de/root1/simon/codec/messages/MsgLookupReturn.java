package de.root1.simon.codec.messages;
/**
 * Lookup return message
 *
 * @author ACHR
 */
public class MsgLookupReturn extends AbstractMessage {
	
    private static final long serialVersionUID = 7371210248110219946L;

    private Class<?>[] value;

    public MsgLookupReturn() {
    }

    public Class<?>[] getInterfaces() {
        return value;
    }

    public void setInterfaces(Class<?>[] value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return getSequence() + ":MsgLookupReturn(" + value + ')';
    }
}