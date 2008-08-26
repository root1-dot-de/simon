package de.root1.simon.tests.server;


import java.io.IOException;
import java.net.UnknownHostException;

import de.root1.simon.Simon;

public class SampleServer {
	
	public static void main(String[] args) throws InterruptedException, IllegalStateException, IOException {
			
//		Utils.DEBUG = true;
		
		ServerInterfaceImpl serverImpl = new ServerInterfaceImpl();
		Simon.createRegistry(2000);
		Simon.bind("server", serverImpl);
		System.out.println("Server gestartet");
				
	}

}
