/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test.interfacelookup;

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

/**
 *
 * @author achristian
 */
public class TestInterfaceLookup {

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

            System.out.println("Begin interfaceLookupAndReleaseTwice...");
            RemoteObjectImpl roi = new RemoteObjectImpl();

            Registry r = Simon.createRegistry(22222);
            r.bind("roi", roi);

            System.out.println("bound roi to registry ...");
            Lookup lookup = Simon.createInterfaceLookup("localhost", 22222);

            System.out.println("canonical interface name: "+RemoteObject.class.getCanonicalName());

            RemoteObject roiRemote = (RemoteObject) lookup.lookup(RemoteObject.class.getCanonicalName());

            System.out.println("roi lookup done");

            roiRemote.helloWorld();
            System.out.println("method call done");

            lookup.release(roiRemote);
            System.out.println("release of roi done");

            r.unbind("roi");
            System.out.println("unbind of roi done");
            r.stop();
            System.out.println("registry stopped");

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