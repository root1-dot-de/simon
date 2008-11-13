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
import de.root1.simon.exceptions.NameBindingException;
import de.root1.simon.tests.server.ServerInterfaceImpl;

/**
 * TODO document me
 * 
 * @author ACHR
 * 
 */
public class RegistryTest extends TestCase {

	private ServerInterfaceImpl serverImpl = new ServerInterfaceImpl();
	private Registry registry;
	
	public RegistryTest(String name) {
		super(name);
	}

	// initial for each test setup
	protected void setUp() {
		try {
			registry = Simon.createRegistry(InetAddress.getLocalHost(),2000);
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

//	public void testEmptyTest(){
//		
//	}
	
	public void testCreateIndividualRegistry2Times() {

		try {
			Simon.createRegistry(InetAddress.getLocalHost(),22222);
			new AssertionError("creating a second individual registry with the same port must fail with an BindException/IOException");
		} catch (UnknownHostException e) {
			new AssertionError("Testing is only possible on system where at least localhost is useable!");
		} catch (BindException e) {
			// this is expected
		} catch (IOException e){
			new AssertionError("this shouldn't happen");
		}

	}
	
	public void testNameBindingException (){
		try {
			System.out.println("trying to bind to registry. registry="+registry);
			registry.bind("myServer", serverImpl);
		} catch (NameBindingException e) {
			new AssertionError("bindung a remoteobject the first time, there should not be an exception");
		}
//		
//		try {
//			registry.bind("myServer", serverImpl);
//		} catch (NameBindingException e) {
//			// this is expected
//		}
	}


};