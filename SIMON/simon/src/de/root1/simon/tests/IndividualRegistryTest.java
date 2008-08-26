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

import org.junit.Test;

import de.root1.simon.Simon;
import de.root1.simon.tests.server.ServerInterfaceImpl;
import junit.framework.TestCase;

/**
 * TODO document me
 * 
 * @author ACHR
 * 
 */
public class IndividualRegistryTest extends TestCase {

	ServerInterfaceImpl serverImpl = new ServerInterfaceImpl();
	
	public IndividualRegistryTest(String name) {
		super(name);
	}

	// initial for each test setup
	protected void setUp() {
		try {
			Simon.createRegistry(InetAddress.getLocalHost(),2000);
		} catch (UnknownHostException e) {
			new AssertionError("localhost must be present!");
		} catch (IllegalStateException e) {
			new AssertionError("the first time the registry is created, there should not be any IllegalStateException while creating the registry!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// tear down after each test
	protected void tearDown() {
		Simon.shutdownRegistry();
	}

	// -----------------------
	// TESTS
	// -----------------------

	public void testCreateIndividualRegistry2Times() {

		try {
			Simon.createRegistry(InetAddress.getLocalHost(),2000);
			new AssertionError("creating a second individual registry with the same port must fail with an BindException/IOException");
		} catch (UnknownHostException e) {
			new AssertionError("Testing is only possible on system where at least localhost is useable!");
		} catch (BindException e) {
			// this is expected
		} catch (IOException e){
			new AssertionError("this shouldn't happen");
		}
		

	}


};