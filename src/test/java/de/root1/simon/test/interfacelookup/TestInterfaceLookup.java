/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test.interfacelookup;

import de.root1.simon.InterfaceLookup;
import de.root1.simon.Lookup;
import de.root1.simon.Registry;
import de.root1.simon.Simon;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author achristian
 */
public class TestInterfaceLookup {
    
    private final Logger logger = LoggerFactory.getLogger(TestInterfaceLookup.class);

    public TestInterfaceLookup() {
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

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void interfaceLookupAndReleaseTwice() {

        try {

            logger.info("Begin interfaceLookupAndReleaseTwice...");
            RemoteObjectImpl roi = new RemoteObjectImpl();

            Registry r = Simon.createRegistry(22222);
            r.start();
            r.bind("roi", roi);

            logger.info("bound roi to registry ...");
            Lookup lookup = Simon.createInterfaceLookup("localhost", 22222);

            logger.info("canonical interface name: "+RemoteObject.class.getCanonicalName());

            RemoteObject roiRemote = (RemoteObject) lookup.lookup(RemoteObject.class.getCanonicalName());

            logger.info("roi lookup done");

            roiRemote.helloWorld();
            logger.info("method call done");

            lookup.release(roiRemote);
            logger.info("release of roi done");
            
            lookup.release(roiRemote);
            logger.info("Awaiting network connections shutdown");
            ((InterfaceLookup)lookup).awaitCompleteShutdown(30000);
            logger.info("Awaiting network connections shutdown *done*");

            r.unbind("roi");
            logger.info("unbind of roi done");
            r.stop();
            logger.info("registry stopped");

            // ----------------

            RemoteObjectImpl roi2 = new RemoteObjectImpl();
            Registry r2 = Simon.createRegistry(22222);
            r2.bind("roi2", roi2);
            Lookup lookup2 = Simon.createInterfaceLookup("localhost", 22222);

            

            RemoteObject roiRemote2 = (RemoteObject) lookup.lookup(RemoteObject.class.getCanonicalName());
            roiRemote2.helloWorld();
            lookup2.release(roiRemote2);

            r2.unbind("roi2");
            r2.stop();

            assert true;

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new AssertionError(ex);
        }
    }

}