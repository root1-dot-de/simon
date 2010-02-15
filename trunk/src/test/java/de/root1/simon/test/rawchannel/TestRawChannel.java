/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test.rawchannel;

import de.root1.simon.Lookup;
import de.root1.simon.RawChannel;
import de.root1.simon.Registry;
import de.root1.simon.Simon;
import de.root1.simon.SimonRemote;
import de.root1.simon.exceptions.EstablishConnectionFailed;
import de.root1.simon.exceptions.LookupFailedException;
import de.root1.simon.exceptions.NameBindingException;
import de.root1.simon.exceptions.SimonRemoteException;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.LogManager;
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
    private static final String TEMPDIR = System.getProperty("java.io.tmpdir")+System.getProperty("file.separator");
    private static final String TESTFILE_RECEIVER = TEMPDIR + "TestFileForReceiver.dat";
    private static final String TESTFILE_SENDER = TEMPDIR + "TestFile.dat";

    private Registry registry;

    public TestRawChannel() {

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

        System.out.println("Generating test file ...");
        try {
            FileChannel fc = new FileOutputStream(new File(TESTFILE_SENDER)).getChannel();

            Random r = new Random(System.currentTimeMillis());

            byte[] data = new byte[1024]; // 1KiB


            for (int i=0;i<10;i++){ // 10 x 1KiB = 10 MiB
                r.nextBytes(data);
                fc.write(ByteBuffer.wrap(data));
            }
            fc.close();
            System.out.println("Generating test file ...*done*");

        } catch (FileNotFoundException ex) {
            // cannot occur as we create this file...
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @After
    public void tearDown() {
        registry.unbind(BIND_NAME);
        registry.stop();

        File f1 = new File(TESTFILE_SENDER);
        f1.delete();

        File f2 = new File(TESTFILE_RECEIVER);
        f2.delete();
    }

    
    @Test
    public void openTransferAndClose() {
        try {

            System.out.println("Doing lookup ...");
            Lookup lookup = Simon.createNameLookup(InetAddress.getLocalHost(), 2000);
            RawChannelServer rcs;
                rcs = (RawChannelServer) lookup.lookup(BIND_NAME);
            System.out.println("Doing lookup ... *done*");

            assertTrue("looked up remote must not be null", rcs!=null);

            int token = rcs.openFileChannel(TESTFILE_RECEIVER);

            System.out.println("open raw channel on server ...");
            RawChannel rawChannel = Simon.openRawChannel(token, rcs);
            System.out.println("open raw channel on server ... *done*");

            File f = new File(TESTFILE_SENDER);

            long fileLength = f.length();

            System.out.println("Test file size: "+fileLength+" bytes");

            DataInputStream dis = new DataInputStream(new FileInputStream(f));

            byte[] fileBytesToBeSend = new byte[(int)fileLength];

            dis.readFully(fileBytesToBeSend);
            dis.close();

            FileChannel fc = new FileInputStream(f).getChannel();

            ByteBuffer data = ByteBuffer.allocate(512);
            System.out.println("Transfering file to server ...");
            while (fc.read(data) != -1) {
                System.out.println("   transfering data: "+data);
                rawChannel.write(data);
                data.clear();
            }
            System.out.println("Transfering file to server ... *done*");
            fc.close();
            rawChannel.close();

            System.out.println("Receiving sent file from server ...");
            byte[] fileBytesReceived = rcs.getFileBytes(TESTFILE_RECEIVER);
            System.out.println("Receiving sent file from server ... *done*");

            assertTrue("Received filesize must match set filesize", fileBytesReceived.length==fileBytesToBeSend.length);

            boolean byteMatch = false;
            System.out.println("Comparing sent file with received file ...");
            for (int i=0;i<fileBytesToBeSend.length;i++) {
                assertTrue("sent byte no. "+i+" must match received byte no. "+i, fileBytesToBeSend[i]==fileBytesReceived[i]);
            }
            System.out.println("Comparing sent file with received file ... *done*");
            lookup.release(rcs);

        } catch (UnknownHostException ex) {
            System.out.println("UnknownHostException occured!");
            new AssertionError("An UnknownHostException occured which should not be the case with localhost comunication.");
        } catch (LookupFailedException ex) {
            System.out.println("Lookup failed!");
            Logger.getLogger(TestRawChannel.class.getName()).log(Level.SEVERE, null, ex);
            new AssertionError("A LookupFailedException occured which should not be the case in test case.");
        } catch (SimonRemoteException ex) {
            System.out.println("SRE occured!");
            Logger.getLogger(TestRawChannel.class.getName()).log(Level.SEVERE, null, ex);
            new AssertionError("A unexcepted SimonRemoteException occured which should not be the case.");
        } catch (IOException ex) {
            System.out.println("IOE occured!");
            Logger.getLogger(TestRawChannel.class.getName()).log(Level.SEVERE, null, ex);
            new AssertionError("A unexcepted IOException occured which should not be the case in test case.");
        } catch (EstablishConnectionFailed ex) {
            Logger.getLogger(TestRawChannel.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
            new AssertionError("Establishing connection failed during test run ..."+ex.getMessage());
        }
        System.out.println("test done");
    }

}