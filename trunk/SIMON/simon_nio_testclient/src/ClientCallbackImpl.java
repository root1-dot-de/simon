

import de.root1.simon.exceptions.SimonRemoteException;

public class ClientCallbackImpl implements ClientCallbackInterface {

	private static final long serialVersionUID = 1L;

	public void callback(String text) throws SimonRemoteException {
		System.out.println("Die folgende Meldung stammt vom Server: "+text);
	}

}
