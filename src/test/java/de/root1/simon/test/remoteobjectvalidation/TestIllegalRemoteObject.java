/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.root1.simon.test.remoteobjectvalidation;

import de.root1.simon.test.interfacelookup.*;
import de.root1.simon.Lookup;
import de.root1.simon.Registry;
import de.root1.simon.Simon;
import de.root1.simon.exceptions.IllegalRemoteObjectException;
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
public class TestIllegalRemoteObject {

    public TestIllegalRemoteObject() {
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
    public void testCallbackMissingInterface() {

        Registry registry = null;
        try {

            Server server = new Server();

            registry = Simon.createRegistry(22222);
            registry.bind("server", server);
            System.out.println("Server ready.");

            System.out.println("bound roi to registry ...");
            Lookup lookup = Simon.createNameLookup("localhost", 22222);

            IServer iServer = (IServer) lookup.lookup("server");

            IClient clientWithoutInterface = new ClientWithoutInterface();
            IClient clientWithInterface = new ClientWithInterface();
            
            try {
                iServer.test(clientWithoutInterface);
                new AssertionError("It's not allowed to have a callback object without a declared interface");
            } catch (IllegalRemoteObjectException ex) {
                System.out.println("Got expected error: "+ex+" --> SUCCESS");
            }
            
            iServer.test(clientWithInterface);

            lookup.release(iServer);

            registry.unbind("server");
            registry.stop();
            System.out.println("registry stopped");

            assert true;

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new AssertionError(ex);
        } finally {
            if (registry!=null) registry.stop();
        }
    }
}
