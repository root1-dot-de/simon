package de.root1.simon.tests.shared;


import de.root1.simon.SimonRemote;
import de.root1.simon.exceptions.SimonRemoteException;

public interface ClientCallbackInterface extends SimonRemote {
	
	public void callback(String text) throws SimonRemoteException;

}
