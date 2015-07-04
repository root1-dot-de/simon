/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.root1.simon.test.remotepublication;

import de.root1.simon.test.remoteobjectvalidation.*;
import de.root1.simon.Lookup;
import de.root1.simon.NameLookup;
import de.root1.simon.PublicationSearcher;
import de.root1.simon.Registry;
import de.root1.simon.Simon;
import de.root1.simon.SimonPublication;
import de.root1.simon.exceptions.IllegalRemoteObjectException;
import java.net.InetSocketAddress;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author achristian
 */
public class TestRemotePublication {

    private final Logger logger = LoggerFactory.getLogger(TestRemotePublication.class);

    public TestRemotePublication() {
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
    public void testRemotePublication() {

        Registry localRegistry = null;
        Registry remoteRegistry = null;
        try {

            ServerInterface server = new ServerInterfaceImpl();

            localRegistry = Simon.createRegistry(22222);
            localRegistry.start();
            
            remoteRegistry = Simon.createRegistry(22223);
            remoteRegistry.start();
            remoteRegistry.startRemotePublishingService();
            
            InetSocketAddress remoteSockAddr = new InetSocketAddress("localhost", 22223);
            
            localRegistry.bindAndPublishRemote("server", server, remoteSockAddr);
            
            
            logger.info("Server bound to local registry and published on remote registry");

            List<SimonPublication> remotePublications = localRegistry.getRemotePublications(remoteSockAddr, "server");
            for (SimonPublication publication : remotePublications) {
                System.out.println("found: "+publication);
            }
            
            

            
            localRegistry.unbind("server");
            remoteRegistry.stopRemotePublishingService();
            
            localRegistry.stop();
            remoteRegistry.stop();
            
            assert true;

        } catch (Exception ex) {
            throw new AssertionError(ex);
        } finally {
            if (localRegistry != null) {
                localRegistry.stop();
            }
        }
    }
}
