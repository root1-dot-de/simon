package de.root1.simon.tests.client;



import java.io.IOException;

import de.root1.simon.Simon;
import de.root1.simon.exceptions.EstablishConnectionFailed;
import de.root1.simon.exceptions.LookupFailedException;
import de.root1.simon.exceptions.SimonRemoteException;
import de.root1.simon.tests.shared.ServerInterface;
import de.root1.simon.tests.shared.ServerSessionInterface;
import de.root1.simon.utils.Utils;

public class SampleClient {
	
	public static void main(String[] args) throws SimonRemoteException, IOException, InterruptedException, EstablishConnectionFailed, LookupFailedException {
		
		
//		Utils.DEBUG = true;
		
		// Callbackobjekt anlegen
		ClientCallbackImpl clientCallbackImpl = new ClientCallbackImpl();
		System.out.println("Callback Objekt angelegt");
		
		System.out.println("Callback2 Objekt angelegt");
		
		
		ServerInterface server = (ServerInterface) Simon.lookup("localhost", 2000, "server");
		ServerSessionInterface serverSession = server.login(clientCallbackImpl);
		
		System.out.println("equals="+serverSession.equals(true));
		
		Simon.release(serverSession);
		Simon.release(server);
	}

}
