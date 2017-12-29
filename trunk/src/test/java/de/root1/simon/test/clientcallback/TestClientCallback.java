/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test.clientcallback;

import de.root1.simon.Lookup;
import de.root1.simon.Registry;
import de.root1.simon.Simon;
import de.root1.simon.test.PortNumberGenerator;
import java.util.logging.Level;
import org.junit.After;
import static org.junit.Assert.assertFalse;
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
public class TestClientCallback {
    
    private final Logger logger = LoggerFactory.getLogger(TestClientCallback.class);
    private int PORT = 0;

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
        PORT = PortNumberGenerator.getNextPort();        
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

            Registry r = Simon.createRegistry(PORT);
            r.start();
            r.bind("roi", roi);

            logger.info("bound roi to registry ...");
            Lookup lookup = Simon.createNameLookup("127.0.0.1", PORT);

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

            Registry r = Simon.createRegistry(PORT);
            r.start();
            r.bind("roi", roi);

            logger.info("bound roi to registry ...");
            Lookup lookup = Simon.createNameLookup("127.0.0.1", PORT);

            RemoteObject roiRemote = (RemoteObject) lookup.lookup("roi");

            logger.info("roi lookup done");

            ClientCallbackImpl cci = new ClientCallbackImpl();
            roiRemote.setCallback(cci);
            
            try {
                ClientCallback callback = roiRemote.getCallback();
                logger.info(">>>>>>>>>>>>>>>>>>>> Saying Hello");
                System.out.flush();
                callback.sayHello();
                System.out.flush();
                logger.info("<<<<<<<<<<<<<<<<<<<< Saying Hello *DONE*");
                logger.info("Got callback back from server --> SUCCESS");
                        
            } catch (Exception e) {
                e.printStackTrace();
                throw new AssertionError("sending local endpoints should work");
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
    @Ignore //TODO remove!
    public void testSendCallbackBackToRemote() {
        try {

            logger.info("Begin ...");
            RemoteObjectImpl roi = new RemoteObjectImpl();

            Registry r = Simon.createRegistry(PORT);
            r.start();
            r.bind("roi", roi);

            logger.info("bound roi to registry ...");
            Lookup lookup = Simon.createNameLookup("127.0.0.1", PORT);

            RemoteObject roiRemote = (RemoteObject) lookup.lookup("roi");

            try {
                logger.info("roi lookup done");
                Session sessionObject = roiRemote.getSessionObject();
                logger.info("Client got session from server: ");
                logger.info("session#{}",sessionObject.getId());
                logger.info(">>>>>>>>>>>>>>>>>>>> sending session back");
                System.out.flush();
                roiRemote.setSessionObject(sessionObject);
                System.out.flush();
                logger.info("<<<<<<<<<<<<<<<<<<<< sending session back *DONE*");
            
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("sending local endpoints should work", e);
                throw new AssertionError("sending local endpoints should work", e);
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

            Registry r = Simon.createRegistry(PORT);
            r.start();
            r.bind("roi", roi);

            logger.info("bound roi to registry ...");
            Lookup lookup = Simon.createNameLookup("127.0.0.1", PORT);

            RemoteObject roiRemote = (RemoteObject) lookup.lookup("roi");

            logger.info("roi lookup done");

            ClientCallbackImpl cci = new ClientCallbackImpl();
            roiRemote.setCallback(cci);
            
            try {
                roiRemote.sendCallbackViaCallback();
                logger.info("sendCallbackViaCallback --> SUCCESS");
            } catch (Exception e) {
                e.printStackTrace();
                throw new AssertionError("sending local endpoints should not throw an exception");
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
    public void testClientCallbackEquals() {
        try {

            logger.info("Begin testClientCallbackEquals ...");
            RemoteObjectImpl roi = new RemoteObjectImpl();

            Registry r = Simon.createRegistry(PORT);
            r.start();
            r.bind("roi", roi);

            logger.info("bound roi to registry ...");
            Lookup lookup = Simon.createNameLookup("127.0.0.1", PORT);

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

            r.unbind("roi");
            logger.info("unbind of roi done");
            r.stop();
            logger.info("registry stopped");

            assert true;

        } catch (Exception ex) {
            throw new AssertionError(ex);
        }
    }
    
    /**
     * see: http://dev.root1.de/issues/102
     */
    @Test
    public void testGetRemoteFromRemote() {
        try {

            logger.info("Begin ...");
            RemoteObjectImpl roi = new RemoteObjectImpl();

            Registry r = Simon.createRegistry(PORT);
            r.start();
            r.bind("roi", roi);

            logger.info("bound roi to registry ...");
            Lookup lookup = Simon.createNameLookup("127.0.0.1", PORT);

            RemoteObject roiRemote = (RemoteObject) lookup.lookup("roi");

            logger.info("roi lookup done");

            try {
                roiRemote.getRemoteObject();
                throw new AssertionError("Getting bound remote objects via the remote itself should throw an Exception.");
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

}