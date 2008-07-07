


import java.io.IOException;

import de.root1.simon.Simon;
import de.root1.simon.exceptions.SimonRemoteException;
import de.root1.simon.utils.Utils;

public class SampleClient {
	
	public static void main(String[] args) throws SimonRemoteException, IOException, InterruptedException {
		
		
		Utils.DEBUG = true;
		
		// Callbackobjekt anlegen
		ClientCallbackImpl clientCallbackImpl = new ClientCallbackImpl();
		System.out.println("Callback Objekt angelegt");
		
		
		ServerInterface server = (ServerInterface) Simon.lookup("localhost", 2000, "server");
		ServerSessionInterface serverSession = server.login(clientCallbackImpl);
		
		while(true) {
			serverSession.printMessageOnServer("Hello World ...");
		}
		
		
		
//		Thread.sleep(10000);
//		server = null;
//		System.gc();
		
//		try {
//			
////			ServerSession serverSession1 = server.login(clientCallbackImpl);
////			ServerSession serverSession2 = server.login(clientCallbackImpl);
////			System.out.println(serverSession1);
////			System.out.println(serverSession2);
//			
//			serverSession1.printMessageOnServer("Hallo du da auf dem Server");
//		} catch (ConnectionException e) {
//			System.out.println("connection is broken");
//			e.printStackTrace();
//		}
	}

}
