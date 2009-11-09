package de.root1.simon.test;

import java.util.concurrent.Semaphore;

import org.junit.Test;

import de.root1.simon.Registry;
import de.root1.simon.Simon;

public class TestThreadedLookup {

    private static final int TESTING_THREADS = 10;
    private volatile Registry registry = null;
    private volatile Semaphore semaphore = new Semaphore(TESTING_THREADS);

    @Test
    public void testThreadedLookup() throws Exception {
        RemoteObjectImpl roi = new RemoteObjectImpl();

        System.out.println("Creating registry");
        registry = Simon.createRegistry(22222);
        registry.bind("roi", roi);

        Thread[] threads = new Thread[TESTING_THREADS];

        System.out.println("Creating threads and blocking semaphore");
        for (int i = 0; i < TESTING_THREADS; i++) {
            threads[i] = new Thread(new ThreadderSchredder(semaphore));
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
    }

    private class ThreadderSchredder implements Runnable {

        private final Semaphore semaphore;

        public ThreadderSchredder(Semaphore semaphore) {
            this.semaphore = semaphore;
        }

        public void run() {
            try {
                RemoteObject roiRemote = (RemoteObject) Simon.lookup("localhost", 22222, "roi");
                roiRemote.helloWorldArg(String.valueOf(Thread.currentThread().getId()));
                Simon.release(roiRemote);
            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                System.out.println("Releasing one semaphore aquire");
                semaphore.release();
            }
        }
    }
}
