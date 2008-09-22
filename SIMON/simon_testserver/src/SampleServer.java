

import java.io.IOException;
import java.net.InetAddress;

import de.root1.simon.Registry;
import de.root1.simon.Simon;
import de.root1.simon.exceptions.NameBindingException;
import de.root1.simon.utils.Utils;

public class SampleServer {
	
	public static void main(String[] args) throws InterruptedException, IllegalStateException, IOException, NameBindingException {

		// for enabling debug mode
//		Utils.DEBUG = true;

		// create the server implementation
		ServerInterfaceImpl serverImpl = new ServerInterfaceImpl();
		
		// create the registry where we bind the server implementation
		Registry serverRegistry = Simon.createRegistry(InetAddress.getByName("127.0.0.1"), 2000);
		
		// and finally bind the implementation
		serverRegistry.bind("MyHiddenSampleServer", serverImpl);
		
		// for demonstration, bind the same object with another name, but also publish it
		serverRegistry.bindAndPublish("MyPublicSampleServer", serverImpl);
		
		// to stop the server, call stop() on the registry
		//serverRegistry.stop();
				
	}

}
