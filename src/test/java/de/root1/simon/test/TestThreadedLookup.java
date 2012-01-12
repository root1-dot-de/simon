package de.root1.simon.test;

import de.root1.simon.Lookup;
import de.root1.simon.Registry;
import de.root1.simon.Simon;
import java.util.concurrent.Semaphore;
import org.junit.Test;

public class TestThreadedLookup {

    private static final int TESTING_THREADS = 100;
    private volatile Registry registry = null;
    private volatile Semaphore semaphore = new Semaphore(TESTING_THREADS);

    @Test
    public void testThreadedLookup() throws Exception {

        long start = System.currentTimeMillis();

        RemoteObjectImpl roi = new RemoteObjectImpl();

        System.out.println("Creating registry");
        registry = Simon.createRegistry(22223);
        registry.bind("roi", roi);

        Thread[] threads = new Thread[TESTING_THREADS];


        for (int i = 0; i < TESTING_THREADS; i++) {
            System.out.println("Creating threads and blocking semaphore: #"+i);
            threads[i] = new Thread(new ThreadderSchredder(semaphore, i));
            semaphore.acquire();
        }

        System.out.println("Starting threads");
        for (int i = 0; i < TESTING_THREADS; i++) {
            threads[i].start();
        }

        semaphore.acquire(TESTING_THREADS);

        System.out.println("Closing registry");
        registry.unbind("roi");
        registry.stop();
        System.out.println("duration=" + (System.currentTimeMillis() - start));
    }

    private class ThreadderSchredder implements Runnable {

        private final Semaphore semaphore;
        private final int i;

        public ThreadderSchredder(Semaphore semaphore, int i) {
            this.semaphore = semaphore;
            this.i = i;
        }

        public void run() {
//            System.out.println("Running thread #"+i+" Free: "+semaphore.availablePermits());
            String done = "";
            try {
                //                System.out.println("Running thread #"+i+" lookup");
                done = "creating namelookup";
                Lookup lookup = Simon.createNameLookup("localhost", 22223);
                done = "creating namelookup *done* doing lookup";
                RemoteObject roiRemote = (RemoteObject) lookup.lookup("roi");
                done = "creating namelookup *done* doing lookup *done* remote call";
//                System.out.println("Running thread #"+i+" lookup *done*");
                roiRemote.helloWorldArg(String.valueOf(i));
                done = "creating namelookup *done* doing lookup *done* remote call *done* release";
//                System.out.println("Running thread #"+i+" invoke *done*");
                lookup.release(roiRemote);
                done = "creating namelookup *done* doing lookup *done* remote call *done* release *done*";
//                System.out.println("Running thread #"+i+" release *done*");
            } catch (Throwable t) {
                System.err.println("########### "+done);
                System.err.flush();
                t.printStackTrace();
                System.err.println("################################");
            } finally {
//                System.out.println("Releasing one semaphore aquire: #"+i+". Free: "+semaphore.availablePermits());
                semaphore.release();
//                System.out.println("Released one semaphore aquire: #"+i+". Free: "+semaphore.availablePermits());
            }

        }
    }
}
