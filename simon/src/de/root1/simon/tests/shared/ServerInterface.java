package de.root1.simon.tests.shared;


import de.root1.simon.SimonRemote;
import de.root1.simon.exceptions.SimonRemoteException;

public interface ServerInterface extends SimonRemote {
	
	public ServerSessionInterface login(ClientCallbackInterface clientCallback) throws SimonRemoteException;
	
}
