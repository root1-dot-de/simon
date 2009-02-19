package de.root1.simon.tests.server;

import de.root1.simon.Simon;
import de.root1.simon.exceptions.SimonRemoteException;
import de.root1.simon.tests.shared.ClientCallbackInterface;
import de.root1.simon.tests.shared.ServerInterface;
import de.root1.simon.tests.shared.ServerSessionInterface;

public class ServerInterfaceImpl implements ServerInterface {

	private static final long serialVersionUID = 1L;

	public ServerSessionInterface login(ClientCallbackInterface clientCallback) throws SimonRemoteException {
		clientCallback.callback("login callback: Dies ist der Callback. " +
				"Deine Adresse lautet "+Simon.getRemoteInetSocketAddress(clientCallback)+" "+
				"und du bist verbunden auf dem lokalen Port "+Simon.getRemoteInetSocketAddress(clientCallback).getPort());
		System.out.println("login callback: Hallo Welt auf dem Server: "+clientCallback);
		return new ServerSessionImpl(clientCallback);
	}
	
	
}
