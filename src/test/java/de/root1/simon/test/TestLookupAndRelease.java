/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test;

import de.root1.simon.Registry;
import de.root1.simon.Simon;
import de.root1.simon.SimonRemote;
import de.root1.simon.exceptions.LookupFailedException;
import de.root1.simon.exceptions.NameBindingException;
import de.root1.simon.exceptions.SessionException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author achristian
 */
public class TestLookupAndRelease {

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

            System.out.println("Begin ...");
            RemoteObjectImpl roi = new RemoteObjectImpl();

            Registry r = Simon.createRegistry(22222);
            r.bind("roi", roi);
            
            RemoteObject roiRemote = (RemoteObject) Simon.lookup("localhost", 22222, "roi");
            roiRemote.helloWorld();
            Simon.release(roiRemote);

            r.unbind("roi");
            r.stop();

            // ----------------

            RemoteObjectImpl roi2 = new RemoteObjectImpl();
            Registry r2 = Simon.createRegistry(22222);
            r2.bind("roi2", roi2);

            RemoteObject roiRemote2 = (RemoteObject) Simon.lookup("localhost", 22222, "roi2");
            roiRemote2.helloWorld();
            Simon.release(roiRemote2);

            r2.unbind("roi2");
            r2.stop();

            assert true;

        } catch (Exception ex) {
            throw new AssertionError(ex);
        }
    }

}