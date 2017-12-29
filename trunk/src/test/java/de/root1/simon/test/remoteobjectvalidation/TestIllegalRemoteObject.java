/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.root1.simon.test.remoteobjectvalidation;

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
 * @author achristian
 */
public class TestIllegalRemoteObject {

    private final Logger logger = LoggerFactory.getLogger(TestIllegalRemoteObject.class);
    private int PORT = 0;

    public TestIllegalRemoteObject() {
    }

    //@BeforeClass
    //public static void setUpClass() throws Exception {
    //}
    //@AfterClass
    //public static void tearDownClass() throws Exception {
    //}
    @Before
    public void setUp() {
        PORT = PortNumberGenerator.getNextPort();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testCallbackMissingInterface() {

        Registry registry = null;
        try {

            Server server = new Server();

            registry = Simon.createRegistry(PORT);
            registry.start();
            registry.bind("server", server);
            logger.info("Server ready.");

            logger.info("bound roi to registry ...");
            Lookup lookup = Simon.createNameLookup("127.0.0.1", PORT);

            IServer iServer = (IServer) lookup.lookup("server");

            IClient clientWithoutInterface = new ClientWithoutInterface();
            IClient clientWithInterface = new ClientWithInterface();

            try {
                iServer.test(clientWithoutInterface);
                throw new AssertionError("It's not allowed to have a callback object without a declared interface");
            } catch (IllegalRemoteObjectException ex) {
                logger.info("Got expected error: {} --> SUCCESS", ex);
            }

            iServer.test(clientWithInterface);

            lookup.release(iServer);

            logger.info("Awaiting network connections shutdown");
            ((NameLookup) lookup).awaitCompleteShutdown(30000);
            logger.info("Awaiting network connections shutdown *done*");

            registry.unbind("server");
            registry.stop();
            logger.info("registry stopped");

            assert true;

        } catch (Exception ex) {
            throw new AssertionError(ex);
        } finally {
            if (registry != null) {
                registry.stop();
            }
        }
    }
}
