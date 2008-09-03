import de.root1.simon.SimonUnreferenced;
import de.root1.simon.exceptions.SimonRemoteException;

public class ServerSessionImpl implements ServerSessionInterface, SimonUnreferenced {

	private static final long serialVersionUID = 1L;
	private String clientString;
	
	public ServerSessionImpl(ClientCallbackInterface clientCallback) {
		this.clientString = clientCallback.toString();
	}

	public void printMessageOnServer(String msg) throws SimonRemoteException {
		System.out.println("Message from Client: "+msg);
	}

	@Override
	public void unreferenced() {
		System.out.println("ServerSessionImpl got unreferenced! -> Related ClientCallbackInterface: "+clientString);
	}

}
