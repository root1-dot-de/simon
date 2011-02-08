/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test.clientcallback;

import de.root1.simon.Lookup;
import de.root1.simon.Registry;
import de.root1.simon.Simon;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.LogManager;
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

        File f = new File("target/test-classes/simon_logging.properties");
        try {
            FileInputStream is = new FileInputStream(f);
            LogManager.getLogManager().readConfiguration(is);


        } catch (FileNotFoundException e) {

                System.err.println("File not found: "+f.getAbsolutePath()+".\n" +
                                "If you don't want to debug SIMON, leave 'Utils.DEBUG' with false-value.\n" +
                                "Otherwise you have to provide a Java Logging API conform properties-file like mentioned.");

        } catch (SecurityException e) {

                System.err.println("Security exception occured while trying to load "+f.getAbsolutePath()+"\n" +
                                "Logging with SIMON not possible!.");

        } catch (IOException e) {

                System.err.println("Cannot load "+f.getAbsolutePath()+" ...\n" +
                                "Please make sure that Java has access to that file.");

        }

        try {

            System.out.println("Begin ...");
            RemoteObjectImpl roi = new RemoteObjectImpl();

            Registry r = Simon.createRegistry(22222);
            r.bind("roi", roi);

            System.out.println("bound roi to registry ...");
            Lookup lookup = Simon.createNameLookup("localhost", 22222);

            RemoteObject roiRemote = (RemoteObject) lookup.lookup("roi");

            System.out.println("roi lookup done");

            ClientCallbackImpl cci = new ClientCallbackImpl();
            roiRemote.setCallback(cci);

            r.unbind("roi");
            System.out.println("unbind of roi done");
            r.stop();
            System.out.println("registry stopped");

            assert true;

        } catch (Exception ex) {
            throw new AssertionError(ex);
        }
    }
    
    @Test
    public void testGetCallbackBackFromRemote() {
        try {

            System.out.println("Begin ...");
            RemoteObjectImpl roi = new RemoteObjectImpl();

            Registry r = Simon.createRegistry(22222);
            r.bind("roi", roi);

            System.out.println("bound roi to registry ...");
            Lookup lookup = Simon.createNameLookup("localhost", 22222);

            RemoteObject roiRemote = (RemoteObject) lookup.lookup("roi");

            System.out.println("roi lookup done");

            ClientCallbackImpl cci = new ClientCallbackImpl();
            roiRemote.setCallback(cci);
            
            try {
                roiRemote.getCallback();
                throw new AssertionError("sending local endpoints should throw an exception");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Got exception: "+e+" --> SUCCESS");
            }
            
            r.unbind("roi");
            System.out.println("unbind of roi done");
            r.stop();
            System.out.println("registry stopped");

            assert true;

        } catch (Exception ex) {
            throw new AssertionError(ex);
        }
    }
    
    @Test
    public void testSendCallbackViaCallback() {
        try {

            System.out.println("Begin ...");
            RemoteObjectImpl roi = new RemoteObjectImpl();

            Registry r = Simon.createRegistry(22222);
            r.bind("roi", roi);

            System.out.println("bound roi to registry ...");
            Lookup lookup = Simon.createNameLookup("localhost", 22222);

            RemoteObject roiRemote = (RemoteObject) lookup.lookup("roi");

            System.out.println("roi lookup done");

            ClientCallbackImpl cci = new ClientCallbackImpl();
            roiRemote.setCallback(cci);
            
            try {
                roiRemote.sendCallbackViaCallback();
                throw new AssertionError("sending local endpoints should throw an exception");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Got exception: "+e+" --> SUCCESS");
            }

            r.unbind("roi");
            System.out.println("unbind of roi done");
            r.stop();
            System.out.println("registry stopped");

            assert true;

        } catch (Exception ex) {
            throw new AssertionError(ex);
        }
    }
    
    @Test
    public void testClientCallbackEquals() {
        try {

            System.out.println("Begin testClientCallbackEquals ...");
            RemoteObjectImpl roi = new RemoteObjectImpl();

            Registry r = Simon.createRegistry(22222);
            r.bind("roi", roi);

            System.out.println("bound roi to registry ...");
            Lookup lookup = Simon.createNameLookup("localhost", 22222);

            RemoteObject roiRemote = (RemoteObject) lookup.lookup("roi");

            System.out.println("roi lookup done");

            ClientCallbackImpl cci = new ClientCallbackImpl();
//            boolean result = roiRemote.testEquals(cci);
            System.out.println("Calling equals on local remoteobject");
            boolean result = roiRemote.equals(roiRemote);
            System.out.println("Calling equals *DONE*");
            assertTrue("On clientside, a remote obejct must be equals to itself", result);
            
            System.out.println("Calling equals on server side");
            result = roiRemote.testEquals(cci);
            System.out.println("Calling equals *DONE*");
            assertTrue("On serverside, a clientcallback-object must be equals to itself", result);
            
            
            System.out.println("Calling equals on remote");
            result = roiRemote.equals(cci);
            System.out.println("Calling equals *DONE*");
            assertFalse("On serverside, a clientcallback-object must be equals to the server remote object", result);
            
            System.out.println("Calling equals on remote with null");
            result = roiRemote.equals(null);
            System.out.println("Calling equals *DONE*");
            assertFalse("On serverside, a null-object must not be equals to the server remote object", result);

            r.unbind("roi");
            System.out.println("unbind of roi done");
            r.stop();
            System.out.println("registry stopped");

            assert true;

        } catch (Exception ex) {
            throw new AssertionError(ex);
        }
    }

}