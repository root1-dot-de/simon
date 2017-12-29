/*
 * Copyright (C) 2008 Alexander Christian <alex(at)root1.de>. All rights reserved.
 * 
 * This file is part of SIMON.
 *
 *   SIMON is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SIMON is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with SIMON.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.root1.simon.test.rawchannel;

import de.root1.simon.RawChannel;
import de.root1.simon.*;
import de.root1.simon.exceptions.EstablishConnectionFailed;
import de.root1.simon.exceptions.LookupFailedException;
import de.root1.simon.exceptions.NameBindingException;
import de.root1.simon.exceptions.SimonRemoteException;
import de.root1.simon.exceptions.RawChannelException;
import de.root1.simon.test.PortNumberGenerator;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Random;
import java.util.logging.Level;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author achristian
 */
public class TestRawChannel {

    private final Logger logger = LoggerFactory.getLogger(TestRawChannel.class);
    private static final String BIND_NAME = "RawChannelTest";
    private static final String TEMPDIR = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator");
    private static final String TESTFILE_RECEIVER = TEMPDIR + "TestFileForReceiver.dat";
    private static final String TESTFILE_SENDER = TEMPDIR + "TestFile.dat";
    private Registry registry;
    private int PORT = 0;

//    @BeforeClass
//    public static void setUpClass() throws Exception {
//    }
//    @AfterClass
//    public static void tearDownClass() throws Exception {
//    }
    @Before
    public void setUp() {
        PORT = PortNumberGenerator.getNextPort();
        try {
            registry = Simon.createRegistry(PORT);
            registry.start();
            logger.info("Registry created");
            RawChannelServerImpl rcsi = new RawChannelServerImpl();
            registry.bind(BIND_NAME, rcsi);
            logger.info("remote bound");
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
            throw new AssertionError("Unable to setup test");
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new AssertionError("Unable to setup test");
        } catch (NameBindingException ex) {
            ex.printStackTrace();
            throw new AssertionError("Unable to setup test");
        }

        logger.info("Generating test file ...");
        try {
            FileChannel fc = new FileOutputStream(new File(TESTFILE_SENDER)).getChannel();

            Random r = new Random(System.currentTimeMillis());

            byte[] data = new byte[1024]; // 1KiB


            for (int i = 0; i < 10; i++) { // 10 x 1KiB = 10 MiB
                r.nextBytes(data);
                fc.write(ByteBuffer.wrap(data));
            }
            fc.close();
            logger.info("Generating test file ...*done*");

        } catch (FileNotFoundException ex) {
            throw new AssertionError("Unable to setup test");
        } catch (IOException ex) {
            throw new AssertionError("Unable to setup test");
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

            logger.info("Doing lookup ...");
            Lookup lookup = Simon.createNameLookup("127.0.0.1", PORT);
            RawChannelServer rcs = (RawChannelServer) lookup.lookup(BIND_NAME);
            logger.info("Doing lookup ... *done*");

            assertTrue("looked up remote must not be null", rcs != null);

            int token = rcs.openFileChannel(TESTFILE_RECEIVER);

            logger.info("open raw channel on server ...");
            RawChannel rawChannel = Simon.openRawChannel(token, rcs);
            logger.info("open raw channel on server ... *done*");

            File f = new File(TESTFILE_SENDER);

            long fileLength = f.length();

            logger.info("Test file size: " + fileLength + " bytes");

            DataInputStream dis = new DataInputStream(new FileInputStream(f));

            byte[] fileBytesToBeSend = new byte[(int) fileLength];

            dis.readFully(fileBytesToBeSend);
            dis.close();

            FileChannel fc = new FileInputStream(f).getChannel();

            ByteBuffer data = ByteBuffer.allocate(512);
            logger.info("Transfering file to server ...");
            while (fc.read(data) != -1) {
                logger.info("   transfering data: {}", data);
                rawChannel.write(data);
                data.clear();
            }
            logger.info("Transfering file to server ... *done*");
            fc.close();
            rawChannel.close();

            logger.info("Receiving sent file from server ...");
            byte[] fileBytesReceived = rcs.getFileBytes(TESTFILE_RECEIVER);
            logger.info("Receiving sent file from server ... *done*");

            assertTrue("Received filesize must match set filesize", fileBytesReceived.length == fileBytesToBeSend.length);

            boolean byteMatch = false;
            logger.info("Comparing sent file with received file ...");
            for (int i = 0; i < fileBytesToBeSend.length; i++) {
                assertTrue("sent byte no. " + i + " must match received byte no. " + i, fileBytesToBeSend[i] == fileBytesReceived[i]);
            }
            logger.info("Comparing sent file with received file ... *done*");
            lookup.release(rcs);

            logger.info("Awaiting network connections shutdown");
            ((NameLookup) lookup).awaitCompleteShutdown(30000);
            logger.info("Awaiting network connections shutdown *done*");

        } catch (UnknownHostException ex) {
            throw new AssertionError("An UnknownHostException occured which should not be the case with localhost comunication.");
        } catch (LookupFailedException ex) {
            throw new AssertionError("A LookupFailedException occured which should not be the case in test case.");
        } catch (SimonRemoteException ex) {
            throw new AssertionError("A unexcepted SimonRemoteException occured which should not be the case.");
        } catch (IOException ex) {
            throw new AssertionError("A unexcepted IOException occured which should not be the case in test case.");
        } catch (EstablishConnectionFailed ex) {
            throw new AssertionError("Establishing connection failed during test run ...");
        } catch (IllegalStateException ex) {
            throw new AssertionError("Establishing connection failed during test run ...");
        } catch (RawChannelException ex) {
            throw new AssertionError("Establishing connection failed during test run ...");
        }
        logger.info("test done");
    }
}