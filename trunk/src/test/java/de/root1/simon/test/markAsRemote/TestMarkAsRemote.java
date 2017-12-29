/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.root1.simon.test.markAsRemote;

import de.root1.simon.InterfaceLookup;
import de.root1.simon.Lookup;
import de.root1.simon.NameLookup;
import de.root1.simon.Registry;
import de.root1.simon.Simon;
import de.root1.simon.exceptions.IllegalRemoteObjectException;
import de.root1.simon.test.PortNumberGenerator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ACHR
 */
public class TestMarkAsRemote {

    private final Logger logger = LoggerFactory.getLogger(TestMarkAsRemote.class);
    private int PORT = 0;

    @Before
    public void setUp() {
        PORT = PortNumberGenerator.getNextPort();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testMarkAndBindAndUseName() {
        logger.info("Running Test");
        try {
            Pojo p = new Pojo();
            IPojo markedAsRemote = (IPojo) Simon.markAsRemote(p);
            Registry registry = Simon.createRegistry(PORT);
            registry.start();

            registry.bind("test", markedAsRemote);
            Lookup lookup = Simon.createNameLookup("127.0.0.1", PORT);

            Object o = lookup.lookup("test");
            logger.info("o={}", o);
            IPojo remotePojo = (IPojo) o;

            System.out.println(remotePojo.getHelloName("Tester"));
            remotePojo.printHelloName("Testung");

            lookup.release(remotePojo);
            logger.info("Awaiting network connections shutdown");
            ((NameLookup) lookup).awaitCompleteShutdown(30000);
            logger.info("Awaiting network connections shutdown *done*");

            registry.unbind("test");
            registry.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        logger.info("Running Test *DONE*");
    }

    @Test
    public void testMarkAndBindAndUseInterface() {
        logger.info("Running Test");
        try {
            Pojo p = new Pojo();
            IPojo markedAsRemote = (IPojo) Simon.markAsRemote(p);
            Registry registry = Simon.createRegistry(PORT);
            registry.start();

            registry.bind("test", markedAsRemote);
            InterfaceLookup lookup = (InterfaceLookup) Simon.createInterfaceLookup("127.0.0.1",PORT);
            Object o = lookup.lookup(IPojo.class.getCanonicalName());
            logger.info("o={}", o);
            IPojo remotePojo = (IPojo) o;

            System.out.println(remotePojo.getHelloName("Tester"));
            remotePojo.printHelloName("Testung");

            lookup.release(remotePojo);
            logger.info("Awaiting network connections shutdown");
            ((InterfaceLookup) lookup).awaitCompleteShutdown(30000);
            logger.info("Awaiting network connections shutdown *done*");

            registry.unbind("test");
            registry.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        logger.info("Running Test *DONE*");
    }

    @Test
    public void testMarkWithoutAnyInterface() {
        logger.info("Running Test");
        try {
            Simon.markAsRemote(this);
            throw new AssertionError("Objects without interface are not allowed/useable!");
        } catch (IllegalRemoteObjectException ex) {

        }
        logger.info("Running Test *DONE*");
    }

}
