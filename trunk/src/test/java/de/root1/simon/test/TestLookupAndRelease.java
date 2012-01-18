/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test;

import de.root1.simon.Lookup;
import de.root1.simon.NameLookup;
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
public class TestLookupAndRelease {
    
    private final Logger logger = LoggerFactory.getLogger(TestLookupAndRelease.class);

    public TestLookupAndRelease() {
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
    public void LookupAndReleaseTwice() {

        try {

            logger.info("Begin ...");
            RemoteObjectImpl roi = new RemoteObjectImpl();

            Registry r = Simon.createRegistry(33333);
            r.bind("roi", roi);

            logger.info("bound roi to registry ...");
            Lookup lookup = Simon.createNameLookup("localhost", 33333);

            RemoteObject roiRemote = (RemoteObject) lookup.lookup("roi");

            logger.info("roi lookup done");

            roiRemote.helloWorld();
            logger.info("method call done");

            lookup.release(roiRemote);
            logger.info("release of roi done");
            
            logger.info("Awaiting network connections shutdown");
            ((NameLookup) lookup).awaitCompleteShutdown(30000);
            logger.info("Awaiting network connections shutdown *done*");

            r.unbind("roi");
            logger.info("unbind of roi done");
            r.stop();
            logger.info("registry stopped");

            // ----------------
            // Wait a few sec to give the OS soe time to really release the socket
            Thread.sleep(5000);

            RemoteObjectImpl roi2 = new RemoteObjectImpl();
            Registry r2 = Simon.createRegistry(33333);
            r2.bind("roi2", roi2);
            Lookup lookup2 = Simon.createNameLookup("localhost", 33333);

            RemoteObject roiRemote2 = (RemoteObject) lookup.lookup("roi2");
            roiRemote2.helloWorld();
            lookup2.release(roiRemote2);
            
            logger.info("Awaiting network connections shutdown");
            ((NameLookup) lookup).awaitCompleteShutdown(30000);
            logger.info("Awaiting network connections shutdown *done*");
            

            r2.unbind("roi2");
            r2.stop();

            assert true;

        } catch (Exception ex) {
            throw new AssertionError(ex);
        }
    }

}