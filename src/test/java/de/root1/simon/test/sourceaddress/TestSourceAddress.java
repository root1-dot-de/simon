/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test.sourceaddress;

import de.root1.simon.InterfaceLookup;
import de.root1.simon.Lookup;
import de.root1.simon.Registry;
import de.root1.simon.Simon;
import de.root1.simon.test.PortNumberGenerator;
import java.net.Inet4Address;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author achristian
 */
public class TestSourceAddress {
    
    private final Logger logger = LoggerFactory.getLogger(TestSourceAddress.class);
    private int PORT = 0;

    public TestSourceAddress() {
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

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void useDifferentSourceAddress() {

        try {

            logger.info("Begin useDifferentSourceAddress...");
            RemoteObjectImpl roi = new RemoteObjectImpl();

            Registry r = Simon.createRegistry(PORT);
            r.start();
            r.bind("roi", roi);

            logger.info("bound roi to registry ...");
            Lookup lookup = Simon.createInterfaceLookup("127.0.0.1",PORT);

            logger.info("canonical interface name: "+RemoteObject.class.getCanonicalName());

            lookup.setSourceAddress(Inet4Address.getByName("127.0.1.1"));
            RemoteObject roiRemote = (RemoteObject) lookup.lookup(RemoteObject.class.getCanonicalName());

            logger.info("roi lookup done");

            String ping = roiRemote.ping(new ClientCallbackImpl());
            logger.info("method call done: {}", ping);

            lookup.release(roiRemote);
            logger.info("release of roi done");
            
            lookup.release(roiRemote);
            logger.info("Awaiting network connections shutdown");
            ((InterfaceLookup)lookup).awaitCompleteShutdown(30000);
            logger.info("Awaiting network connections shutdown *done*");
            
            
            // ----
            
            
            Lookup lookup2 = Simon.createInterfaceLookup("127.0.0.1",PORT);

            logger.info("canonical interface name: "+RemoteObject.class.getCanonicalName());

            RemoteObject roiRemote2 = (RemoteObject) lookup2.lookup(RemoteObject.class.getCanonicalName());

            logger.info("roi lookup done");

            String ping2 = roiRemote2.ping(new ClientCallbackImpl());
            logger.info("method call done: {}", ping2);

            lookup2.release(roiRemote);
            logger.info("release of roi done");
            
            lookup.release(roiRemote);
            logger.info("release of roi done");
            
            logger.info("Awaiting network connections shutdown");
            ((InterfaceLookup)lookup).awaitCompleteShutdown(5000);
            logger.info("Awaiting network connections shutdown *done*");

            r.unbind("roi");
            logger.info("unbind of roi done");
            r.stop();
            logger.info("registry stopped");

            Assert.assertNotEquals("Sourceadresses MUST differ", ping, ping2);
            
            assert true;

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new AssertionError(ex);
        }
    }

}