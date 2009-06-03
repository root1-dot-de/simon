package de.root1.simon.tests.manual;

import de.root1.simon.SimonRemote;
import de.root1.simon.exceptions.SimonRemoteException;

public interface IServer extends SimonRemote {
	
	public String helloServerWorld() throws SimonRemoteException;
	
}