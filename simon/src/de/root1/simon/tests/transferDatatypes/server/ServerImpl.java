package de.root1.simon.tests.transferDatatypes.server;

import java.util.Hashtable;

import de.root1.simon.exceptions.SimonRemoteException;
import de.root1.simon.tests.transferDatatypes.shared.Dummyobject;
import de.root1.simon.tests.transferDatatypes.shared.IServer;

public class ServerImpl implements IServer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void transfer1(Dummyobject myDummyobject,
			Hashtable<String, String> myHashtable) throws SimonRemoteException {
		System.out.println("public void transfer1(Dummyobject myDummyobject, Hashtable<String, String> myHashtable) throws SimonRemoteException");
	}

}
