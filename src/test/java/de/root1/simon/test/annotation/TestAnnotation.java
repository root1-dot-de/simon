/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test.annotation;

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
public class TestAnnotation {

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

//        File f = new File("target/test-classes/simon_logging.properties");
//        try {
//            FileInputStream is = new FileInputStream(f);
//            LogManager.getLogManager().readConfiguration(is);
//
//
//        } catch (FileNotFoundException e) {
//
//                System.err.println("File not found: "+f.getAbsolutePath()+".\n" +
//                                "If you don't want to debug SIMON, leave 'Utils.DEBUG' with false-value.\n" +
//                                "Otherwise you have to provide a Java Logging API conform properties-file like mentioned.");
//
//        } catch (SecurityException e) {
//
//                System.err.println("Security exception occured while trying to load "+f.getAbsolutePath()+"\n" +
//                                "Logging with SIMON not possible!.");
//
//        } catch (IOException e) {
//
//                System.err.println("Cannot load "+f.getAbsolutePath()+" ...\n" +
//                                "Please make sure that Java has access to that file.");
//
//        }
        try {
            RemoteObjectImpl roi = new RemoteObjectImpl();

            Registry r = Simon.createRegistry(22223);
            r.bind("roi", roi);
            Lookup lookup = Simon.createNameLookup("localhost", 22223);

            RemoteObject1 roiRemote = (RemoteObject1) lookup.lookup("roi");

            roiRemote.myRemoteMethod1();

            lookup.release(roiRemote);

            r.unbind("roi");
            r.stop();
        
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new AssertionError("It should be able to access an exported interface. ExceptionMsg: "+ex.getMessage());
        }

    }

    @Test
    public void testUnexportedInterface() {

//        File f = new File("target/test-classes/simon_logging.properties");
//        try {
//            FileInputStream is = new FileInputStream(f);
//            LogManager.getLogManager().readConfiguration(is);
//
//
//        } catch (FileNotFoundException e) {
//
//                System.err.println("File not found: "+f.getAbsolutePath()+".\n" +
//                                "If you don't want to debug SIMON, leave 'Utils.DEBUG' with false-value.\n" +
//                                "Otherwise you have to provide a Java Logging API conform properties-file like mentioned.");
//
//        } catch (SecurityException e) {
//
//                System.err.println("Security exception occured while trying to load "+f.getAbsolutePath()+"\n" +
//                                "Logging with SIMON not possible!.");
//
//        } catch (IOException e) {
//
//                System.err.println("Cannot load "+f.getAbsolutePath()+" ...\n" +
//                                "Please make sure that Java has access to that file.");
//
//        }

        try {
            RemoteObjectImpl roi = new RemoteObjectImpl();

            Registry r = Simon.createRegistry(22224);
            r.bind("roi", roi);
            Lookup lookup = Simon.createNameLookup("localhost", 22224);

            RemoteObject3 roiRemote = (RemoteObject3) lookup.lookup("roi");
            if (roiRemote!=null)
                throw new AssertionError("It should not be possible to access a interface that is not exported.");

            roiRemote.myRemoteMethod3();

            lookup.release(roiRemote);

            r.unbind("roi");
            r.stop();

        } catch (Exception ex) {
            System.out.println("Success! Can't call myMethod3(). ExceptionMsg: "+ex.getMessage());
        }

    }


}