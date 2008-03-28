package de.root1.simon;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ProcessHandler {
	
	/**
	 * 
	 * processes a request for a "hashCode()" call on a remote-object
	 * 
	 * @param requestID
	 * @param remoteObjectName
	 * @throws IOException
	 */
	private void processHashCode(int requestID, String remoteObjectName) throws IOException {
		synchronized (objectOutputStream) {
			objectOutputStream.write(Statics.HASHCODE_RETURN_PACKET);
			objectOutputStream.writeInt(requestID);
			
			final int hashcode = lookupTable.getRemoteBinding(remoteObjectName).hashCode();		
			
			objectOutputStream.writeInt(hashcode);
			objectOutputStream.flush();
		}	
	}
	
	/**
	 * 
	 *processes a request for a "toString()" call on a remote-object
	 * 
	 * @param requestID
	 * @param remoteObjectName
	 * @throws IOException
	 */
	private void processToString(int requestID, String remoteObjectName) throws IOException {
		synchronized (objectOutputStream) {
			objectOutputStream.write(Statics.TOSTRING_RETURN_PACKET);
			objectOutputStream.writeInt(requestID);
			
			final String tostring = lookupTable.getRemoteBinding(remoteObjectName).toString();		
			
			objectOutputStream.writeUTF(tostring);
			objectOutputStream.flush();
		}	
	}
	
	/**
	 * 
	 * processes a request for a "equls()" call on a remote-object
	 * 
	 * @param requestID
	 * @param remoteObjectName
	 * @praram object
	 * @throws IOException
	 */
	private void processEquals(int requestID, String remoteObjectName, Object object) throws IOException{

		ByteBuffer bb = ByteBuffer.allocate(6);
		bb.put(Statics.EQUALS_RETURN_PACKET);
		bb.putInt(requestID);
		final boolean equals = lookupTable.getRemoteBinding(remoteObjectName).equals(object);		
		bb.put(equals ? (byte) 1 : (byte) 0);
		
	}
	
	/**
	 * 
	 * processes a lookup
	 * @throws IOException 
	 */
	private void processLookup(int requestID, String remoteObjectName) throws IOException{
		
		synchronized (objectOutputStream) {
			objectOutputStream.write(Statics.LOOKUP_RETURN_PACKET);
			objectOutputStream.writeInt(requestID);
			objectOutputStream.writeObject(lookupTable.getRemoteBinding(remoteObjectName).getClass().getInterfaces());
			objectOutputStream.flush();
		}	

	}
	


}
