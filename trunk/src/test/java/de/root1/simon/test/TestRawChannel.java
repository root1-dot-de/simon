/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test;

import de.root1.simon.RawChannel;
import de.root1.simon.Registry;
import de.root1.simon.Simon;
import de.root1.simon.SimonRemote;
import de.root1.simon.exceptions.LookupFailedException;
import de.root1.simon.exceptions.NameBindingException;
import de.root1.simon.exceptions.SimonRemoteException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author achristian
 */
public class TestRawChannel {
    private static final String BIND_NAME = "RawChannelTest";

    private Registry registry;

    public TestRawChannel() {
    }

//    @BeforeClass
//    public static void setUpClass() throws Exception {
//    }

//    @AfterClass
//    public static void tearDownClass() throws Exception {
//    }

    @Before
    public void setUp() {
        try {
            registry = Simon.createRegistry(InetAddress.getLocalHost(), 2000);
            System.out.println("Registry created");
            RawChannelServerImpl rcsi = new RawChannelServerImpl() ;
            registry.bind( BIND_NAME, rcsi);
            System.out.println("remote bound");
        } catch (UnknownHostException ex) {
            Logger.getLogger(TestRawChannel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TestRawChannel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NameBindingException ex) {
            Logger.getLogger(TestRawChannel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @After
    public void tearDown() {
        registry.unbind(BIND_NAME);
        registry.stop();
    }

    
    @Test
    public void openTransferAndClose() {
        try {

            System.out.println("Doing lookup ...");
            RawChannelServer rcs = (RawChannelServer) Simon.lookup(InetAddress.getLocalHost(), 2000, BIND_NAME);
            System.out.println(".. *done*");
            assertTrue("looked up remote has to be non-null", rcs!=null);

            int token = rcs.openFileChannel("TestFileName");

            RawChannel rawChannel = Simon.openRawChannel(token, rcs);

            ByteBuffer bb = ByteBuffer.allocate(4);
            bb.put((byte)0x00);
            bb.put((byte)0x01);
            bb.put((byte)0x02);
            bb.put((byte)0x03);

            rawChannel.write(bb);
            rawChannel.close();

            Simon.release(rcs);

        } catch (UnknownHostException ex) {
            System.out.println("UnknownHostException occured!");
        } catch (LookupFailedException ex) {
            System.out.println("Lookup failed!");
            Logger.getLogger(TestRawChannel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SimonRemoteException ex) {
            System.out.println("SRE occured!");
            Logger.getLogger(TestRawChannel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            System.out.println("IOE occured!");
            Logger.getLogger(TestRawChannel.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("test done");
        assertTrue(true);
    }

}