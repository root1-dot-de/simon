/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test.clientcallback;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import de.root1.simon.Lookup;
import de.root1.simon.NameLookup;
import de.root1.simon.Registry;
import de.root1.simon.Simon;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 *
 * @author achristian
 */
public class TestClientCallback {
    
    private final Logger logger = LoggerFactory.getLogger(TestClientCallback.class);

    public TestClientCallback() {
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
    public void passCallbackToServerTest() {

        try {

            logger.info("Begin ...");
            RemoteObjectImpl roi = new RemoteObjectImpl();

            Registry r = Simon.createRegistry(22222);
            r.bind("roi", roi);

            logger.info("bound roi to registry ...");
            Lookup lookup = Simon.createNameLookup("localhost", 22222);

            RemoteObject roiRemote = (RemoteObject) lookup.lookup("roi");

            logger.info("roi lookup done");

            ClientCallbackImpl cci = new ClientCallbackImpl();
            roiRemote.setCallback(cci);

            r.unbind("roi");
            logger.info("unbind of roi done");
            r.stop();
            logger.info("registry stopped");

            assert true;

        } catch (Exception ex) {
            throw new AssertionError(ex);
        }
    }
    
    @Test
    public void testGetCallbackBackFromRemote() {
        try {

            logger.info("Begin ...");
            RemoteObjectImpl roi = new RemoteObjectImpl();

            Registry r = Simon.createRegistry(22222);
            r.bind("roi", roi);

            logger.info("bound roi to registry ...");
            Lookup lookup = Simon.createNameLookup("localhost", 22222);

            RemoteObject roiRemote = (RemoteObject) lookup.lookup("roi");

            logger.info("roi lookup done");

            ClientCallbackImpl cci = new ClientCallbackImpl();
            roiRemote.setCallback(cci);
            
            try {
                roiRemote.getCallback();
                throw new AssertionError("sending local endpoints should throw an exception");
            } catch (Exception e) {
                e.printStackTrace();
                logger.info("Got exception: "+e+" --> SUCCESS");
            }
            
            r.unbind("roi");
            logger.info("unbind of roi done");
            r.stop();
            logger.info("registry stopped");

            assert true;

        } catch (Exception ex) {
            throw new AssertionError(ex);
        }
    }
    
    @Test
    public void testSendCallbackViaCallback() {
        try {

            logger.info("Begin ...");
            RemoteObjectImpl roi = new RemoteObjectImpl();

            Registry r = Simon.createRegistry(22222);
            r.bind("roi", roi);

            logger.info("bound roi to registry ...");
            Lookup lookup = Simon.createNameLookup("localhost", 22222);

            RemoteObject roiRemote = (RemoteObject) lookup.lookup("roi");

            logger.info("roi lookup done");

            ClientCallbackImpl cci = new ClientCallbackImpl();
            roiRemote.setCallback(cci);
            
            try {
                roiRemote.sendCallbackViaCallback();
                throw new AssertionError("sending local endpoints should throw an exception");
            } catch (Exception e) {
                e.printStackTrace();
                logger.info("Got exception: "+e+" --> SUCCESS");
            }

            lookup.release(roiRemote);
            logger.info("Awaiting network connections shutdown");
            ((NameLookup)lookup).awaitCompleteShutdown(30000);
            logger.info("Awaiting network connections shutdown *done*");
            
            r.unbind("roi");
            logger.info("unbind of roi done");
            r.stop();
            logger.info("registry stopped");
            
            assert true;

        } catch (Exception ex) {
            throw new AssertionError(ex);
        }
    }
    
    @Test
    public void testClientCallbackEquals() {
        try {

            logger.info("Begin testClientCallbackEquals ...");
            RemoteObjectImpl roi = new RemoteObjectImpl();

            Registry r = Simon.createRegistry(22222);
            r.bind("roi", roi);

            logger.info("bound roi to registry ...");
            Lookup lookup = Simon.createNameLookup("localhost", 22222);

            RemoteObject roiRemote = (RemoteObject) lookup.lookup("roi");

            logger.info("roi lookup done");

            ClientCallbackImpl cci = new ClientCallbackImpl();
//            boolean result = roiRemote.testEquals(cci);
            logger.info("Calling equals on local remoteobject");
            boolean result = roiRemote.equals(roiRemote);
            logger.info("Calling equals *DONE*");
            assertTrue("On clientside, a remote obejct must be equals to itself", result);
            
            logger.info("Calling equals on server side");
            result = roiRemote.testEquals(cci);
            logger.info("Calling equals *DONE*");
            assertTrue("On serverside, a clientcallback-object must be equals to itself", result);
            
            
            logger.info("Calling equals on remote");
            result = roiRemote.equals(cci);
            logger.info("Calling equals *DONE*");
            assertFalse("On serverside, a clientcallback-object must be equals to the server remote object", result);
            
            logger.info("Calling equals on remote with null");
            result = roiRemote.equals(null);
            logger.info("Calling equals *DONE*");
            assertFalse("On serverside, a null-object must not be equals to the server remote object", result);

            lookup.release(roiRemote);
            logger.info("Awaiting network connections shutdown");
            ((NameLookup)lookup).awaitCompleteShutdown(30000);
            logger.info("Awaiting network connections shutdown *done*");
            
            r.unbind("roi");
            logger.info("unbind of roi done");
            r.stop();
            logger.info("registry stopped");

            assert true;

        } catch (Exception ex) {
            throw new AssertionError(ex);
        }
    }

}