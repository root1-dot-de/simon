package de.root1.simon.tests.server;


import java.io.IOException;

import de.root1.simon.Registry;
import de.root1.simon.Simon;
import de.root1.simon.exceptions.NameBindingException;

public class SampleServer {
	
	public static void main(String[] args) throws InterruptedException, IllegalStateException, IOException, NameBindingException {
			
//		Utils.DEBUG = true;
		
		ServerInterfaceImpl serverImpl = new ServerInterfaceImpl();
		Registry serverRegistry = Simon.createRegistry(2000);
		serverRegistry.bind("server", serverImpl);
		System.out.println("Server gestartet");
				
	}

}
