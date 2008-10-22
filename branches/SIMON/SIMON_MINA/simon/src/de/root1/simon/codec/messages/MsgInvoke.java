package de.root1.simon.codec.messages;

import java.lang.reflect.Method;

/**
 * <code>INVOKE</code> message
 *
 * @author ACHR
 */
public class MsgInvoke extends AbstractMessage {
    private static final long serialVersionUID = 1L;

    String remoteObjectName;
    Method method;
    Object[] args;
    
    public MsgInvoke() {
    }

    public String getRemoteObjectName() {
        return remoteObjectName;
    }

    public void setRemoteObjectName(String remoteObjectName) {
        this.remoteObjectName = remoteObjectName;
    }
    
    public void setArguments(Object[] args){
    	this.args=args;
    }

    @Override
    public String toString() {
        // it is a good practice to create toString() method on message classes.
        return getSequence() + ":MsgLookup(" + remoteObjectName + "|" + method + "|" + args + ")";
    }

	public void setMethod(Method method) {
		this.method = method;
	}
	
	public Method getMethod(){
		return method;
	}
	
	public Object[] getArguments(){
		return args;
	}
}
