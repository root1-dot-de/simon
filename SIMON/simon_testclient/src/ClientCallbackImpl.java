

import de.root1.simon.SimonUnreferenced;
import de.root1.simon.exceptions.SimonRemoteException;

public class ClientCallbackImpl implements ClientCallbackInterface, SimonUnreferenced {

	private static final long serialVersionUID = 1L;

	public void callback(String text) throws SimonRemoteException {
		System.out.println("ClientCallbackInterface: Die folgende Meldung stammt vom Server: "+text);
	}

	@Override
	public void unreferenced() {
		System.out.println("ClientCallbackImpl got unreferenced ...");
	}

}
