package de.root1.simon.tests.client;


import de.root1.simon.exceptions.SimonRemoteException;
import de.root1.simon.tests.shared.ClientCallbackInterface;

public class ClientCallbackImpl implements ClientCallbackInterface {

	private static final long serialVersionUID = 1L;

	public void callback(String text) throws SimonRemoteException {
		System.out.println("ClientCallbackInterface: Die folgende Meldung stammt vom Server: "+text);
	}

}
