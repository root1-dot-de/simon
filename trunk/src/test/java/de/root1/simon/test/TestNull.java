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
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author achristian
 */
public class TestNull {
    
    private int PORT = 0;

    public TestNull() {
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
    public void testEqualsNull() {

        
        try {
            RemoteObjectImpl roi = new RemoteObjectImpl();

            Registry r = Simon.createRegistry(PORT);
            r.start();
            r.bind("roi", roi);
            Lookup lookup = Simon.createNameLookup("127.0.0.1", PORT);

            RemoteObject roiRemote = (RemoteObject) lookup.lookup("roi");

            roiRemote.equals(null);

            lookup.release(roiRemote);

            r.unbind("roi");
            r.stop();
        
        } catch (Exception ex) {
            throw new AssertionError(ex);
        }

    }

    @Test
    public void testArgNull() {


        try {
            RemoteObjectImpl roi = new RemoteObjectImpl();

            Registry r = Simon.createRegistry(PORT);
            r.start();
            r.bind("roi", roi);
            Lookup lookup = Simon.createNameLookup("127.0.0.1", PORT);

            RemoteObject roiRemote = (RemoteObject) lookup.lookup("roi");

            roiRemote.helloWorldArg(null);

            lookup.release(roiRemote);
            
            r.unbind("roi");
            r.stop();

        } catch (Exception ex) {
            throw new AssertionError(ex);
        }

    }

}