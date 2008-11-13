/*
 * Copyright (C) 2008 Alexander Christian <alex(at)root1.de>. All rights reserved.
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
package de.root1.simon.tests.transferDatatypes;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;

import junit.framework.TestCase;
import de.root1.simon.Registry;
import de.root1.simon.Simon;
import de.root1.simon.exceptions.EstablishConnectionFailed;
import de.root1.simon.exceptions.LookupFailedException;
import de.root1.simon.exceptions.NameBindingException;
import de.root1.simon.exceptions.SimonRemoteException;
import de.root1.simon.tests.transferDatatypes.server.ServerImpl;
import de.root1.simon.tests.transferDatatypes.shared.Dummyobject;
import de.root1.simon.tests.transferDatatypes.shared.IServer;
import de.root1.simon.utils.Utils;

/**
 * Tests transferring different data types
 * 
 * @author ACHR
 * 
 */
public class TransferDatatypesTest extends TestCase {

	private Registry registry;
	
	public TransferDatatypesTest(String name) {
		super(name);
	}

	// initial for each test setup
	protected void setUp() {
		Utils.DEBUG = true;
		try {
			registry = Simon.createRegistry(InetAddress.getLocalHost(),22222);
			registry.bind("server", new ServerImpl());
			System.out.println("Server on 22222 running");
		} catch (UnknownHostException e) {
			e.printStackTrace();
			new AssertionError("localhost must be present!");
		} catch (IllegalStateException e) {
			e.printStackTrace();
			new AssertionError("the first time the registry is created, there should not be any IllegalStateException while creating the registry!");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NameBindingException e) {
			e.printStackTrace();
		}
	}

	// tear down after each test
	protected void tearDown() {
		registry.stop();
		while (registry.isRunning()) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
			}
		}
		Utils.DEBUG = false;
	}

	// -----------------------
	// TESTS
	// -----------------------

	public void testTransferDummyobjectAndHashtable() {
		
		
		Hashtable<String, String> myHashtable = new Hashtable<String, String>();
		
		for (int i=0;i<100;i++) {
			myHashtable.put("myKey"+i, "myValue"+i);
		}
		
		try {
			
			IServer server = (IServer)Simon.lookup(InetAddress.getLocalHost(), 22222, "server");
			
			server.transfer1(new Dummyobject(), myHashtable);
			
			Simon.release(server);
			
		} catch (SimonRemoteException e) {
			e.printStackTrace();
		} catch (EstablishConnectionFailed e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LookupFailedException e) {
			e.printStackTrace();
		}

	}
	


};