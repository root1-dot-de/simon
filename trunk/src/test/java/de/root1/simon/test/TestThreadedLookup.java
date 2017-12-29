package de.root1.simon.test;

import de.root1.simon.Lookup;
import de.root1.simon.Registry;
import de.root1.simon.Simon;
import java.util.concurrent.Semaphore;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestThreadedLookup {

    private final Logger logger = LoggerFactory.getLogger(TestThreadedLookup.class);
    private static final int TESTING_THREADS = 100;
    private volatile Registry registry = null;
    private volatile Semaphore semaphore = new Semaphore(TESTING_THREADS);
    private int PORT = 0;

    @Before
    public void setUp() {
        PORT = PortNumberGenerator.getNextPort();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testThreadedLookup() throws Exception {

        long start = System.currentTimeMillis();

        RemoteObjectImpl roi = new RemoteObjectImpl();

        logger.info("Creating registry");
        registry = Simon.createRegistry(PORT);
        registry.start();
        registry.bind("roi", roi);

        Thread[] threads = new Thread[TESTING_THREADS];

        for (int i = 0; i < TESTING_THREADS; i++) {
            logger.info("Creating threads and blocking semaphore: #" + i);
            threads[i] = new Thread(new ThreadderSchredder(semaphore, i));
            semaphore.acquire();
        }

        logger.info("Starting threads");
        for (int i = 0; i < TESTING_THREADS; i++) {
            threads[i].start();
        }

        semaphore.acquire(TESTING_THREADS);

        logger.info("Closing registry");
        registry.unbind("roi");
        registry.stop();
        logger.info("duration=" + (System.currentTimeMillis() - start));
    }

    private class ThreadderSchredder implements Runnable {

        private final Semaphore semaphore;
        private final int i;

        public ThreadderSchredder(Semaphore semaphore, int i) {
            this.semaphore = semaphore;
            this.i = i;
        }

        public void run() {
            logger.info("Running thread #"+i+" Free: "+semaphore.availablePermits());
            String done = "";
            try {
                //                logger.info("Running thread #"+i+" lookup");
                done = "creating namelookup";
                Lookup lookup = Simon.createNameLookup("127.0.0.1", PORT);
                done = "creating namelookup *done* doing lookup";
                RemoteObject roiRemote = (RemoteObject) lookup.lookup("roi");
                done = "creating namelookup *done* doing lookup *done* remote call";
                logger.info("Running thread #"+i+" lookup *done*");
                roiRemote.helloWorldArg(String.valueOf(i));
                done = "creating namelookup *done* doing lookup *done* remote call *done* release";
                logger.info("Running thread #"+i+" invoke *done*");
                lookup.release(roiRemote);
                done = "creating namelookup *done* doing lookup *done* remote call *done* release *done*";
                logger.info("Running thread #"+i+" release *done*");
            } catch (Throwable t) {
                System.err.println("########### " + done);
                System.err.flush();
                t.printStackTrace();
                System.err.println("################################");
            } finally {
                logger.info("Releasing one semaphore aquire: #"+i+". Free: "+semaphore.availablePermits());
                semaphore.release();
                logger.info("Released one semaphore aquire: #"+i+". Free: "+semaphore.availablePermits());
            }

        }
    }
}
