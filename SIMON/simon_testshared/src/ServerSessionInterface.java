

import de.root1.simon.SimonRemote;
import de.root1.simon.exceptions.SimonRemoteException;

public interface ServerSessionInterface extends SimonRemote {
	
	public void printMessageOnServer(String msg) throws SimonRemoteException;

}
