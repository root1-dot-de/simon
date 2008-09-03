


import java.io.IOException;

import de.root1.simon.Simon;
import de.root1.simon.exceptions.EstablishConnectionFailed;
import de.root1.simon.exceptions.LookupFailedException;
import de.root1.simon.exceptions.SimonRemoteException;
import de.root1.simon.utils.Utils;

public class SampleClient {
	
	public static void main(String[] args) throws SimonRemoteException, IOException, InterruptedException, EstablishConnectionFailed, LookupFailedException {
		
		// for enabling debug mode
		//Utils.DEBUG = true;
		
		// Create the client's callback object
		ClientCallbackImpl clientCallbackImpl = new ClientCallbackImpl();
		
		// lookup the server object
		ServerInterface server = (ServerInterface) Simon.lookup("127.0.0.1", 2000, "server");
		
		// do the login and provide the callback interface to the server
		// as a result, we get a "server callback" object which includes all the methods for 
		// interacting with the server
		ServerSessionInterface serverSession = server.login(clientCallbackImpl);
		
		// call a method on the server's callback object
		serverSession.printMessageOnServer("Hello World");
		
		// release all known remote objects to release the connection to the server
//		Simon.release(serverSession);
//		Simon.release(server);
	}


}
