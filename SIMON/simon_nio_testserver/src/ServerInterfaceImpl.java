import de.root1.simon.Simon;
import de.root1.simon.exceptions.SimonRemoteException;

public class ServerInterfaceImpl implements ServerInterface {

	private static final long serialVersionUID = 1L;

	public ServerSessionInterface login(ClientCallbackInterface clientCallback) throws SimonRemoteException {
		clientCallback.callback("Dies ist der Callback. " +
				"Deine Adresse lautet "+Simon.getRemoteInetAddress(clientCallback)+" "+
				"und du bist verbunden auf dem lokalen Port "+Simon.getRemotePort(clientCallback));
		System.out.println("Hallo Welt auf dem Server");
		return new ServerSessionImpl();
	}
	
}
