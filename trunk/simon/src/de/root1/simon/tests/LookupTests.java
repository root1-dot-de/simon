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
package de.root1.simon.tests;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import junit.framework.TestCase;
import de.root1.simon.Registry;
import de.root1.simon.Simon;
import de.root1.simon.exceptions.EstablishConnectionFailed;
import de.root1.simon.exceptions.LookupFailedException;
import de.root1.simon.exceptions.NameBindingException;
import de.root1.simon.exceptions.SimonRemoteException;
import de.root1.simon.tests.server.ServerInterfaceImpl;
import de.root1.simon.utils.Utils;

/**
 * TODO document me
 * 
 * @author ACHR
 * 
 */
public class LookupTests extends TestCase {

	private ServerInterfaceImpl serverImpl = new ServerInterfaceImpl();
	private Registry registry;
	
	public LookupTests(String name) {
		super(name);
	}

	// initial for each test setup
	protected void setUp() {
		Utils.DEBUG = true;
		try {
			System.out.println("Creating registry ...");
			registry = Simon.createRegistry(InetAddress.getLocalHost(),2000);
			System.out.println("Registry created ...");
			registry.bind("test", serverImpl);
			System.out.println("'test' bound to registry ...");
		} catch (UnknownHostException e) {
			System.err.println("error while RegistryTest#setUp(): localhost must be present!");
			e.printStackTrace();
		} catch (IllegalStateException e) {
			System.err.println("error while RegistryTest#setUp(): the first time the registry is created, there should not be any IllegalStateException while creating the registry!");
			e.printStackTrace();
		} catch (BindException e){
			System.err.println("error while RegistryTest#setUp(): Bindung to ip and port should normally work");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("error while RegistryTest#setUp(): there should be nop IOException");
			e.printStackTrace();
		} catch (NameBindingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		if (registry==null) 
			System.exit(1);
	}

	// tear down after each test
	protected void tearDown() {
		if (registry==null) 
			System.err.println("Error while RegistryTest#tearDown(): registry object should not be NULL");
		registry.stop();
		while (registry.isRunning()) {
			try {
				System.out.println("waiting for registry to shutdown!");
				Thread.sleep(2000);
			} catch (InterruptedException e) {
			}
		}
	}

	// -----------------------
	// TESTS
	// -----------------------

	
	public void testLookupNonExistingObject() {

		boolean lookupFailedException = false;
		try {
			System.out.println("Doing lookup ...");
			Simon.lookup(InetAddress.getLocalHost(), 2000, "test");
			System.out.println("Lookup done ...");
		} catch (EstablishConnectionFailed e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SimonRemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LookupFailedException e) {
			lookupFailedException = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (!lookupFailedException) 
			new AssertionError("Looking up a not existing remote object has to result in a LookupFailedException.");

	}
	
};