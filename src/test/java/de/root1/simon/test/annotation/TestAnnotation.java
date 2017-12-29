/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.root1.simon.test.annotation;

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
public class TestAnnotation {

    private final Logger logger = LoggerFactory.getLogger(TestAnnotation.class);

    public TestAnnotation() {
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
    public void testExportedInterface() {

        try {
            RemoteObjectImpl roi = new RemoteObjectImpl();

            Registry r = Simon.createRegistry(22223);
            r.start();
            r.bind("roi", roi);
            Lookup lookup = Simon.createNameLookup("localhost", 22223);

            RemoteObject1 roiRemote = (RemoteObject1) lookup.lookup("roi");

            roiRemote.myRemoteMethod1();

            lookup.release(roiRemote);

            r.unbind("roi");
            r.stop();

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new AssertionError("It should be able to access an exported interface. ExceptionMsg: " + ex.getMessage());
        }

    }

    @Test
    public void testUnexportedInterface() {

        try {
            RemoteObjectImpl roi = new RemoteObjectImpl();

            Registry r = Simon.createRegistry(22224);
            r.start();
            r.bind("roi", roi);
            Lookup lookup = Simon.createNameLookup("localhost", 22224);

            RemoteObject3 roiRemote = (RemoteObject3) lookup.lookup("roi");
            if (roiRemote != null) {
                throw new AssertionError("It should not be possible to access a interface that is not exported.");
            }

            roiRemote.myRemoteMethod3();

            lookup.release(roiRemote);

            r.unbind("roi");
            r.stop();

        } catch (Exception ex) {
            logger.info("Success! Can't call myMethod3(). ExceptionMsg: " + ex.getMessage());
        }

    }

    @Test
    public void testNestedInterfaceImpl() {
        try {
            ObjectWithNestedInterface o = new ObjectWithNestedInterface();
            ObjectWithNestedInterface.ServerAPI roni = o.createInstance();
            
            Registry r = Simon.createRegistry(22225);
            r.start();
            r.bind("roni", roni);
            Lookup lookup = Simon.createNameLookup("localhost", 22225);

            ObjectWithNestedInterface.ServerAPI roiRemote = (ObjectWithNestedInterface.ServerAPI) lookup.lookup("roni");
            
            roiRemote.stuff();

            lookup.release(roiRemote);

            r.unbind("roni");
            r.stop();
            logger.info("Success! Can't call nested implementation of a remote object().");
            
            
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new AssertionError("It should be able to access an nested implementation of an remote interface. ExceptionMsg: " + ex.getMessage());
        }
    }

}
