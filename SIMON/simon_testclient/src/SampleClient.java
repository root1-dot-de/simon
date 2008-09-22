


import java.io.IOException;
import java.util.List;

import de.root1.simon.PublicationSearcher;
import de.root1.simon.Simon;
import de.root1.simon.SimonPublication;
import de.root1.simon.exceptions.EstablishConnectionFailed;
import de.root1.simon.exceptions.LookupFailedException;
import de.root1.simon.exceptions.SimonRemoteException;
import de.root1.simon.utils.Utils;

public class SampleClient {
	
	public static void main(String[] args) throws SimonRemoteException, IOException, InterruptedException, EstablishConnectionFailed, LookupFailedException {
		
		// for enabling debug mode
		//Utils.DEBUG = true;
		
		// first, have a look at the servers on the local network
		System.out.println("Searching for servers ...");
		PublicationSearcher publicationSearcher = Simon.searchRemoteObjects(null, 2000);
		
		while(publicationSearcher.isSearching()){
			List<SimonPublication> foundServers = publicationSearcher.getNewPublications();
			for (SimonPublication simonPublication : foundServers) {
				System.out.println("Found:" +
						" serverhost="+simonPublication.getAddress()+
						" serverport="+simonPublication.getPort()+
						" remoteObjectName="+simonPublication.getRemoteObjectName());
				System.out.println("Progress: "+publicationSearcher.getSearchProgress()+"%");
			}
		}
		System.out.println("Progress: "+publicationSearcher.getSearchProgress()+"%");
		System.out.println("Search finished");
		
		// now create a connection to a known server
		
		// Create the client's callback object
		ClientCallbackImpl clientCallbackImpl = new ClientCallbackImpl();
		
		// lookup the server object
		ServerInterface server = (ServerInterface) Simon.lookup("127.0.0.1", 2000, "MyHiddenSampleServer");
		
		// do the login and provide the callback interface to the server
		// as a result, we get a "server callback" object which includes all the methods for 
		// interacting with the server
		ServerSessionInterface serverSession = server.login(clientCallbackImpl);
		
		// call a method on the server's callback object
		serverSession.printMessageOnServer("Hello World");
		
		// release all known remote objects to release the connection to the server
		Simon.release(serverSession);
		Simon.release(server);
	}


}
