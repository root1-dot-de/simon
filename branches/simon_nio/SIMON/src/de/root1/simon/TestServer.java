/**
 * TODO Documentation to be done
 */
package de.root1.simon;

import java.io.IOException;

/**
 * TODO Documentation to be done
 *
 * @author achristian
 *
 */
public class TestServer {
	
	public static void main(String[] args) throws IOException {
		
		Statics.DEBUG_MODE = true;
		
		LookupTable lookupTable = new LookupTable();
		lookupTable.putRemoteBinding("server", new SimonRemote() {});
		Acceptor a = new Acceptor(lookupTable);
		Thread t = new Thread(a);
		t.start();
	}

}
