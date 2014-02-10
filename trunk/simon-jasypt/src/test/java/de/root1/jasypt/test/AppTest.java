/*
 * Copyright (C) 2013 Alexander Christian <alex(at)root1.de>. All rights reserved.
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
package de.root1.jasypt.test;

import de.root1.simon.jasypt.JasyptSimonPBE;
import static junit.framework.Assert.assertEquals;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        JasyptSimonPBE js1 = new JasyptSimonPBE("MySecretPw");
        JasyptSimonPBE js2 = new JasyptSimonPBE("MySecretPw");
        JasyptSimonPBE js3 = new JasyptSimonPBE("AnotherSecretPw");
        
        String x = "MyDataString";
        
        System.out.println("x.getBytes().length="+x.getBytes().length);
        
        byte[] encrypted = js1.encrypt(x.getBytes());
        System.out.println("encrypted.length="+encrypted.length);
        byte[] decrypted = js2.decrypt(encrypted);
        System.out.println("decrypted.length="+decrypted.length);
        String y = new String(decrypted);
        System.out.println("y.getBytes().length="+y.getBytes().length);
        
        assertEquals("Encoding+Decoding with same PW should work", x, y);
        
        // --------------
        try {
            byte[] encrypted2 = js1.encrypt(x.getBytes());
            byte[] decrypted2 = js3.decrypt(encrypted2);
            String y2 = new String(decrypted2);
            assertTrue("It should not be possible to decrpyt with different PW", false);
        } catch (EncryptionOperationNotPossibleException e) {
            // expected exception...
            assertTrue(true);
        }
        
    }
}
