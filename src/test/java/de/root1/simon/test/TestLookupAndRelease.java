/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.root1.simon.test;

import de.root1.simon.Lookup;
import de.root1.simon.NameLookup;
import de.root1.simon.Registry;
import de.root1.simon.Simon;
import de.root1.simon.exceptions.EstablishConnectionFailed;
import de.root1.simon.exceptions.LookupFailedException;
import java.net.UnknownHostException;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author achristian
 */
public class TestLookupAndRelease {

    private final Logger logger = LoggerFactory.getLogger(TestLookupAndRelease.class);
    private int PORT = 0;  

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
        PORT = PortNumberGenerator.getNextPort();  logger.info("set up");
        
    }

    @After
    public void tearDown() {
        logger.info("tear down");
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void LookupAndReleaseTwice() {

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

            roiRemote.helloWorld();
            logger.info("method call done");

            lookup.release(roiRemote);
            logger.info("release of roi done");

            logger.info("Awaiting network connections shutdown");
            ((NameLookup) lookup).awaitCompleteShutdown(30000);
            logger.info("Awaiting network connections shutdown *done*");

            r.unbind("roi");
            logger.info("unbind of roi done");
            r.stop();
            logger.info("registry stopped");

            // ----------------
            // Wait a few sec to give the OS soe time to really release the socket
            Thread.sleep(5000);

            RemoteObjectImpl roi2 = new RemoteObjectImpl();
            Registry r2 = Simon.createRegistry(PORT);
            r2.start();
            r2.bind("roi2", roi2);
            Lookup lookup2 = Simon.createNameLookup("127.0.0.1", PORT);

            RemoteObject roiRemote2 = (RemoteObject) lookup.lookup("roi2");
            roiRemote2.helloWorld();
            lookup2.release(roiRemote2);

            logger.info("Awaiting network connections shutdown");
            ((NameLookup) lookup).awaitCompleteShutdown(30000);
            logger.info("Awaiting network connections shutdown *done*");


            r2.unbind("roi2");
            r2.stop();

            assert true;

        } catch (Exception ex) {
            throw new AssertionError(ex);
        }
    }

    @Test
    public void LookupAndReleaseMultipleWithoutSleep() {

        logger.info("Begin ...");
        Registry r = null;
        try {
            RemoteObjectImpl roi = new RemoteObjectImpl();
            r = Simon.createRegistry(PORT);
            r.start();
            r.bind("roi", roi);

            for (int i = 0; i < 20; i++) {

                logger.info("\n"
                        + "***********************************\n"
                        + "********* Run:  {}\n"
                        + "***********************************", i);
                try {


                    logger.info("bound roi to registry ...");
                    Lookup lookup = Simon.createNameLookup("127.0.0.1", PORT);

                    RemoteObject roiRemote = (RemoteObject) lookup.lookup("roi");

                    logger.info("roi lookup done");

                    roiRemote.helloWorld();
                    logger.info("method call done");

                    lookup.release(roiRemote);
                    logger.info("release of roi done");

                    // Mit dieser Zeile hier funktioniert's
                    ((NameLookup) lookup).awaitCompleteShutdown(30000);

                    assert true;

                } catch (Exception ex) {
                    throw new AssertionError(ex);
                }
            }

        } catch (Exception e) {
            throw new AssertionError(e);
        } finally {
            if (r != null) {
                r.unbind("roi");
                logger.info("unbind of roi done");
                r.stop();
                logger.info("registry stopped");
            }
        }
    }

    @Test
    public void testMultithreadedLookupAndRelease() throws Exception {

        RemoteObjectImpl roi = new RemoteObjectImpl();
        Registry r = Simon.createRegistry(PORT);
        r.start();
        r.bind("server", roi);
        int count = 3;
        final Semaphore s = new Semaphore(count);
        s.acquire(count);
        Thread[] threads = new Thread[count];

        for (int i = 0; i < threads.length; i++) {
            final int ii = i+1;
            threads[i] = new Thread(""+ii) {
                @Override
                public void run() {
                    try {
                        String name = Thread.currentThread().getName();
                        logger.info("Thread {} doing lookup", name);
                        Lookup lookup = Simon.createNameLookup("127.0.0.1", PORT);
                        RemoteObject server = (RemoteObject) lookup.lookup("server");
                        logger.info("Thread {} got remote object", name);
                        server.helloWorldArg(Thread.currentThread().getName());
                        logger.info("Thread {} called method", name);
                        try {
                            long sleeptime = ii*2000;
                            logger.info("Thread {} sleeping for {} ms ...", name, sleeptime);
                            Thread.sleep(sleeptime);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                        logger.info("Thread {} doing release", name);
                        lookup.release(server);
                        logger.info("Thread {} ALL DONE", name);
                        s.release();
                    } catch (UnknownHostException ex) {
                        throw new AssertionError(ex);
                    } catch (LookupFailedException ex) {
                        throw new AssertionError(ex);
                    } catch (EstablishConnectionFailed ex) {
                        throw new AssertionError(ex);
                    }

                }
            };
        }
        for (int j = 0; j < threads.length; j++) {
            logger.debug("Starting thread {}", j);
            threads[j].start();
        }
        s.acquire(3);
        logger.debug("done");
        r.stop();

    }
}