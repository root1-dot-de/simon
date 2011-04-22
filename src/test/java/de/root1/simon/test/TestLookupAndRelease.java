/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test;

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

            Registry r = Simon.createRegistry(33333);
            r.bind("roi", roi);

            System.out.println("bound roi to registry ...");
            Lookup lookup = Simon.createNameLookup("localhost", 33333);

            RemoteObject roiRemote = (RemoteObject) lookup.lookup("roi");

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
            // Wait a few sec to give the OS soe time to really release the socket
            Thread.sleep(5000);

            RemoteObjectImpl roi2 = new RemoteObjectImpl();
            Registry r2 = Simon.createRegistry(33333);
            r2.bind("roi2", roi2);
            Lookup lookup2 = Simon.createNameLookup("localhost", 33333);

            RemoteObject roiRemote2 = (RemoteObject) lookup.lookup("roi2");
            roiRemote2.helloWorld();
            lookup2.release(roiRemote2);

            r2.unbind("roi2");
            r2.stop();

            assert true;

        } catch (Exception ex) {
            throw new AssertionError(ex);
        }
    }

}