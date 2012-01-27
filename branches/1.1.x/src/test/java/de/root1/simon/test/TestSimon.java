/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import de.root1.simon.Lookup;
import de.root1.simon.Registry;
import de.root1.simon.Simon;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author achristian
 */
public class TestSimon {

    private final Logger logger = LoggerFactory.getLogger(TestSimon.class);
    
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
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testCreateRegistryTwice() {

        try {
            Registry r = Simon.createRegistry(8888);
            Registry r2 = Simon.createRegistry(8888);
            r.stop();
            r2.stop();
            throw new AssertionError("There should be a BindException in case of running a port is already in use.");
        } catch (Exception ex) {
            // expected!
        }

    }

}