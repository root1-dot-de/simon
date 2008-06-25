import de.root1.simon.exceptions.SimonRemoteException;

public class ServerSessionImpl implements ServerSessionInterface {

	public void printMessageOnServer(String msg) throws SimonRemoteException {
		System.out.println("Message from Client: "+msg);
	}

}
