package de.root1.simon.test;

import java.util.concurrent.Semaphore;

import org.junit.Test;

import de.root1.simon.Registry;
import de.root1.simon.Simon;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.LogManager;

public class TestThreadedLookup {

    private static final int TESTING_THREADS = 100;
    private volatile Registry registry = null;
    private volatile Semaphore semaphore = new Semaphore(TESTING_THREADS);

    @Test
    public void testThreadedLookup() throws Exception {

        long start = System.currentTimeMillis();
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


        RemoteObjectImpl roi = new RemoteObjectImpl();

        System.out.println("Creating registry");
        registry = Simon.createRegistry(22222);
        registry.bind("roi", roi);

        Thread[] threads = new Thread[TESTING_THREADS];

        
        for (int i = 0; i < TESTING_THREADS; i++) {
//            System.out.println("Creating threads and blocking semaphore: #"+i);
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
        System.out.println("duration="+(System.currentTimeMillis()-start));
    }

    private class ThreadderSchredder implements Runnable {

        private final Semaphore semaphore;
        private final int i;

        public ThreadderSchredder(Semaphore semaphore, int i) {
            this.semaphore = semaphore;
            this.i=i;
        }

        public void run() {
//            System.out.println("Running thread #"+i+" Free: "+semaphore.availablePermits());
            try {
//                System.out.println("Running thread #"+i+" lookup");
                RemoteObject roiRemote = null;
                // TODO FIXME
//                (RemoteObject) Simon.lookup("localhost", 22222, "roi");
//                System.out.println("Running thread #"+i+" lookup *done*");
                roiRemote.helloWorldArg(String.valueOf(i));
//                System.out.println("Running thread #"+i+" invoke *done*");
                Simon.release(roiRemote);
//                System.out.println("Running thread #"+i+" release *done*");
            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
//                System.out.println("Releasing one semaphore aquire: #"+i+". Free: "+semaphore.availablePermits());
                semaphore.release();
//                System.out.println("Released one semaphore aquire: #"+i+". Free: "+semaphore.availablePermits());
            }
            
        }
    }
}
