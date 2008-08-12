import de.root1.simon.SimonUnreferenced;
import de.root1.simon.exceptions.SimonRemoteException;

public class ServerSessionImpl implements ServerSessionInterface, SimonUnreferenced {

	private static final long serialVersionUID = 1L;

	public void printMessageOnServer(String msg) throws SimonRemoteException {
		System.out.println("Message from Client: "+msg);
	}

	public void unreferenced() {
		System.out.println("I got unreferenced!");
	}

}
