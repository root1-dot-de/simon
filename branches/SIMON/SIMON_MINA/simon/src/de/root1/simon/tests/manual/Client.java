package de.root1.simon.tests.manual;

import java.io.IOException;
import java.net.UnknownHostException;

import de.root1.simon.Simon;
import de.root1.simon.exceptions.EstablishConnectionFailed;
import de.root1.simon.exceptions.LookupFailedException;
import de.root1.simon.exceptions.SimonRemoteException;
import de.root1.simon.utils.Utils;

public class Client {
	
	public static void main(String[] args) throws UnknownHostException, IOException, SimonRemoteException, EstablishConnectionFailed, LookupFailedException {
		Utils.DEBUG = true;
		IServer simonRemote = (IServer) Simon.lookup("127.0.0.1", 2000, "server");
		simonRemote.helloServerWorld();
		Simon.release(simonRemote);
		
	}

}
