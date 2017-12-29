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
package de.root1.simon.test;

import de.root1.simon.Lookup;
import de.root1.simon.Registry;
import de.root1.simon.Simon;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author achristian
 */
public class TestSimon {

    private final Logger logger = LoggerFactory.getLogger(TestSimon.class);
    private int PORT = 0;
    
    public TestSimon() {
    }

    //@BeforeClass
    //public static void setUpClass() throws Exception {
    //}

    //@AfterClass
    //public static void tearDownClass() throws Exception {
    //}

    @Before
    public void setUp() {
        PORT = PortNumberGenerator.getNextPort();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testDenoteSameRemoteObject() {


        try {
            RemoteObjectImpl roi = new RemoteObjectImpl();

            Registry r = Simon.createRegistry(PORT);
            r.start();
            r.bind("roi", roi);
            Lookup lookup = Simon.createNameLookup("127.0.0.1", PORT);

            RemoteObject roiRemote1 = (RemoteObject) lookup.lookup("roi");
            RemoteObject roiRemote2 = (RemoteObject) lookup.lookup("roi");

            assertTrue("Two remote object instances of same remoteobject must be the same", Simon.denoteSameRemoteObjekt(roiRemote1, roiRemote2));

            lookup.release(roiRemote1);
            lookup.release(roiRemote2);
            
            r.unbind("roi");
            r.stop();

        } catch (Exception ex) {
            throw new AssertionError(ex);
        }

    }
    
    @Test
    //@Ignore // ignored temporarily due to JVM Bug: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=7179799
    public void testCreateRegistryTwice() {


        try {
            Registry r = Simon.createRegistry(PORT);
            r.start();
            Thread.sleep(2000);
            Registry r2 = Simon.createRegistry(PORT);
            r2.start();
            Thread.sleep(500);
            r.stop();
            r2.stop();
            throw new AssertionError("There should be a BindException in case of running a port is already in use.");
        } catch (Exception ex) {
            // expected!
        }

    }

}