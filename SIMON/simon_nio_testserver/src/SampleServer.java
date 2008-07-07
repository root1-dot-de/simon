

import de.root1.simon.Simon;
import de.root1.simon.utils.Utils;

public class SampleServer {
	
	public static void main(String[] args) throws InterruptedException {
			
		Utils.DEBUG = true;
		
		ServerInterfaceImpl serverImpl = new ServerInterfaceImpl();
		Simon.createRegistry(2000);
		Simon.bind("server", serverImpl);
		System.out.println("Server gestartet");
		
	}

}
