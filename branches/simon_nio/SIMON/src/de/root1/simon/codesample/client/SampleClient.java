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
package de.root1.simon.codesample.client;

import java.io.IOException;

import de.root1.simon.Simon;
import de.root1.simon.SimonRemoteException;
import de.root1.simon.Statics;
import de.root1.simon.codesample.common.ServerInterface;

public class SampleClient {
	
	public static void main(String[] args) throws SimonRemoteException, IOException {
		
		Statics.DEBUG_MODE = true;
		
		// Callbackobjekt anlegen
		ClientCallbackImpl clientCallbackImpl = new ClientCallbackImpl();
		System.out.println("Callback Objekt angelegt");
		
		
//		ServerInterface server = (ServerInterface) Simon.lookup(args[0], 2000, "server");
		ServerInterface server = (ServerInterface) Simon.lookup("localhost", 2000, "server");
		
		server.login(clientCallbackImpl);
//		System.out.println(Simon.getRemoteInetAddress(server));
//		System.out.println(Simon.getRemotePort(server));
		
	}

}
