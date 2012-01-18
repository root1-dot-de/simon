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
    public void testDenoteSameRemoteObject() {


        try {
            RemoteObjectImpl roi = new RemoteObjectImpl();

            Registry r = Simon.createRegistry(22222);
            r.bind("roi", roi);
            Lookup lookup = Simon.createNameLookup("localhost", 22222);

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

}