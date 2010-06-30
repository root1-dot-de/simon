/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test;

import de.root1.simon.Registry;
import de.root1.simon.Simon;
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
public class TestNull {

    public TestNull() {
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
    public void testEqualsNull() {

        
        try {
            RemoteObjectImpl roi = new RemoteObjectImpl();

            Registry r = Simon.createRegistry(22222);
            r.bind("roi", roi);

            RemoteObject roiRemote = (RemoteObject) Simon.lookup("localhost", 22222, "roi");

            roiRemote.equals(null);

            Simon.release(roiRemote);

            r.unbind("roi");
            r.stop();
        
        } catch (Exception ex) {
            throw new AssertionError(ex);
        }

    }

    @Test
    public void testArgNull() {


        try {
            RemoteObjectImpl roi = new RemoteObjectImpl();

            Registry r = Simon.createRegistry(22222);
            r.bind("roi", roi);

            RemoteObject roiRemote = (RemoteObject) Simon.lookup("localhost", 22222, "roi");

            roiRemote.helloWorldArg(null);

            Simon.release(roiRemote);

            r.unbind("roi");
            r.stop();

        } catch (Exception ex) {
            throw new AssertionError(ex);
        }

    }

}