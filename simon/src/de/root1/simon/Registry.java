/*
 * Copyright 2008 Alexander Christian <alex(at)root1.de>. All rights reserved.
 * 
 * This file is part of SIMON.
 *
 *   SIMON is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SIMON is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with SIMON.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.root1.simon;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * TODO Documentation to be done
 *
 * @author achristian
 *
 */
public class Registry extends Thread {
	
	private LookupTable serverLookupTable = null;
	private ServerSocket server;
	private int port;

	/**
	 * TODO Documentation to be done
	 * @param lookupTable
	 * @param port
	 */
	public Registry(LookupTable lookupTable, int port) {
		this.serverLookupTable = lookupTable;
		this.port = port;
		this.setName("SimonRegistry");
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		if (Statics.DEBUG_MODE) System.out.println("Registry.run() -> start");
		
		try {
			
			// Starte die Registry
			server = new ServerSocket(port);
			
						
		} catch (BindException e){
			System.err.println("Unable to bind to port="+port);
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		while (!interrupted()) {
			// Create an unbound socket
			Socket socket = new Socket();
			
			// Wait for connection from client.
			try {
				Simon.preSetupSocket(socket);
				
				socket = server.accept();
				
				Simon.postSetupSocket(socket);
				
				if (Statics.DEBUG_MODE) System.out.println("Registry.run() -> client connected: "+socket.getInetAddress());
				new Endpoint(socket, Simon.getObjectCacheLifetime(), serverLookupTable, this.getName()).start(); // starting endpoint
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		if (Statics.DEBUG_MODE) System.out.println("Registry.run() -> end");
	}
	
	public void putBinding(String name, SimonRemote remoteObject) {
		serverLookupTable.putRemoteBinding(name, remoteObject);
	}
	
}
