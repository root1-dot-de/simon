package de.root1.simon.tests.transferDatatypes.shared;

import java.util.Hashtable;

import de.root1.simon.SimonRemote;
import de.root1.simon.exceptions.SimonRemoteException;

public interface IServer extends SimonRemote {
	
	public void transfer1(Dummyobject myDummyobject, Hashtable<String, String> myHashtable) throws SimonRemoteException;

}
