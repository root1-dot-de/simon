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
public class RegistryTest extends TestCase {

	ServerInterfaceImpl serverImpl = new ServerInterfaceImpl();
	
	public RegistryTest(String name) {
		super(name);
	}

	// initial for each test setup
	protected void setUp() {
		try {
			Simon.createRegistry(2000);
		} catch (UnknownHostException e) {
			new AssertionError("localhost must be present!");
		} catch (IllegalStateException e) {
			new AssertionError("the first time the registry is created, there should not be any IllegalStateException while creating the registry!");
		}
	}

	// tear down after each test
	protected void tearDown() {
		Simon.shutdownRegistry();
	}

	// -----------------------
	// TESTS
	// -----------------------

	@Test(expected=java.lang.IllegalStateException.class)
	public void testCreateGlobalRegistry2Times() throws UnknownHostException, IllegalStateException {

		Simon.createRegistry(2000);

	}

	public void testBooleanFalse() {

		assertFalse("should not be true", false);

	}

};