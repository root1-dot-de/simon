import de.root1.simon.Simon;
import de.root1.simon.SimonUnreferenced;
import de.root1.simon.exceptions.SimonRemoteException;

public class ServerInterfaceImpl implements ServerInterface, SimonUnreferenced {

	private static final long serialVersionUID = 1L;

	public ServerSessionInterface login(ClientCallbackInterface clientCallback) throws SimonRemoteException {
		clientCallback.callback("login callback: Dies ist der Callback. " +
				"Deine Adresse lautet "+Simon.getRemoteInetAddress(clientCallback)+" "+
				"und du bist verbunden auf dem lokalen Port "+Simon.getRemotePort(clientCallback));
		System.out.println("login callback: Hallo Welt auf dem Server: "+clientCallback);
		return new ServerSessionImpl(clientCallback);
	}

	@Override
	public void unreferenced() {
		System.out.println("ServerInterfaceImpl got unreferenced!");
		
	}
	
}
