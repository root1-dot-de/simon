package de.root1.simon.tests.manual;

import java.io.IOException;


import de.root1.simon.Registry;
import de.root1.simon.Simon;
import de.root1.simon.exceptions.NameBindingException;
import de.root1.simon.utils.Utils;

public class Server {


	
	
	public static void main(String[] args) throws IOException, IOException, NameBindingException {
		Utils.DEBUG = true;
		ISimonImpl server = new ISimonImpl();
		Registry registry = Simon.createRegistry(2000);
		registry.bind("server", server);
		
		
	}

}
