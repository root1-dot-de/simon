/*
 * Copyright (C) 2013 Alexander Christian <alex(at)root1.de>. All rights reserved.
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
package de.root1.simon.filetransmit;

import de.root1.simon.RawChannel;
import de.root1.simon.Simon;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A default implementation to send files to
 * <code>DefaultFileReceiver</code>. You can create your own by overwriting
 * methods, or by creating an own implementation which makes use of server's 
 * <code>FileReceiver</code> implementation.
 *
 * @since 1.2.0
 * @author achristian
 */
public class DefaultFileSender {

    /**
     * The logger used for this class
     */
    private final static Logger logger = LoggerFactory.getLogger(DefaultFileSender.class);
    private int txBLockSize = 8 * 1024; // 8k
    private AtomicInteger sendId = new AtomicInteger(0);
    private FileReceiver fileReceiver;
    private ExecutorService sendPool;
    public List<FileSenderProgressListener> listeners = Collections.synchronizedList(new ArrayList<FileSenderProgressListener>());

    /**
     * Close the file sender. All uploads in progress will continue until
     * completion or in case of error abort.
     */
    public void close() {
        sendPool.shutdown();
    }

    /**
     * Close the file sender and wait at most
     * <code>timeout</code> milliseconds to complete or in case of error abort
     * all downloads
     *
     * @param timeout milliseconds to wait for close completion
     * @return true if this file sender completed all uploads and terminated, or
     * false if the timeout elapsed before termination
     * @throws InterruptedException if interrupted while waiting
     */
    public boolean closeAndWait(int timeout) throws InterruptedException {
        sendPool.shutdown();
        return sendPool.awaitTermination(30 * 1000, TimeUnit.MILLISECONDS);
    }

    /**
     * File sending is done via SendTask which is thrown into the <code>sendPool</code>
     */
    private class SendTask implements Runnable {

        private final File f;
        private int id;
        private final boolean overwriteExisting;

        /**
         * Init the file send operation
         * @param f the file to send
         * @param id a unique ID for the file
         * @param overwriteExisting a flag which is used to tell the server whether any existing file will be overwritten or not
         */
        private SendTask(File f, int id, boolean overwriteExisting) {
            this.f = f;
            this.id = id;
            this.overwriteExisting = overwriteExisting;
        }

        @Override
        public void run() {
            int token;
            long bytesSent = 0;
            RawChannel rawChannel = null;
            try {
                token = fileReceiver.requestChannelToken(f.getName(), f.length(), overwriteExisting);
                logger.debug("FileReceiver provided token {} for file {}", token, f.getName());
                rawChannel = Simon.openRawChannel(token, fileReceiver);

                FileChannel fc = new FileInputStream(f).getChannel();

                // we send the file in 512byte packages through the RawChannel
                ByteBuffer data = ByteBuffer.allocate(getTxBLockSize());
                while (fc.read(data) != -1) {
                    logger.trace("ID={} Sending chunk ...", id);
                    rawChannel.write(data);
                    bytesSent += data.limit();
                    for (FileSenderProgressListener listener : getListenersCopy()) {
                        listener.inProgress(id, f, bytesSent, f.length());
                    }
                    data.clear();
                }
                for (FileSenderProgressListener listener : getListenersCopy()) {
                    listener.completed(id, f);
                }
                if (rawChannel != null) {
                    rawChannel.close();
                }

            } catch (Exception ex) {
                for (FileSenderProgressListener listener : getListenersCopy()) {
                    listener.aborted(id, f, ex);
                }
            } finally {
            }
            logger.debug("ID={} Sending done", id);
        }
    }

    /**
     * Creates a file sender and connects it to the given file receiver. Files will be sent sequentially.
     * @param fileReceiver the receiving object
     */
    public DefaultFileSender(FileReceiver fileReceiver) {
        this.fileReceiver = fileReceiver;
        sendPool = Executors.newFixedThreadPool(1);
    }

    /**
     * Creates a file sender and connects it to the given receiver. 
     * 
     * @param fileReceiver the receiving object
     * @param txThreads number of max. concurrent uploads
     */
    public DefaultFileSender(FileReceiver fileReceiver, int txThreads) {
        this.fileReceiver = fileReceiver;
        sendPool = Executors.newFixedThreadPool(txThreads);
    }

    /**
     * Set the block size for writing data
     * @param blockSize number of bytes to write in one block
     */
    public void setTxBlockSize(int blockSize) {
        this.txBLockSize = blockSize;
    }

    /**
     * Gets the block size for writing data
     * @return  number of bytes to write in one block
     */
    public int getTxBLockSize() {
        return txBLockSize;
    }

    /**
     * Send local file to connected file receiver
     * 
     * @param f the file to send. 
     * @return a generated ID for the file. Can be used to identify the file in progress listener. Especially useful when transmitting files with same names one after another. When Integer.MAX_VALUE is reached, ID is reset back to 0.
     */
    public int sendFile(File f) {
        int id = sendId.getAndIncrement();
        
        if (id == Integer.MAX_VALUE) {
            sendId.set(0);
            id = 0;
        }
        for (FileSenderProgressListener listener : getListenersCopy()) {
            listener.started(id, f, f.length());
        }
        sendPool.execute(new SendTask(f, id, false));
        return id;
    }

    /**
     * @see DefaultFileSender#sendFile(java.io.File) 
     * @param f the file to send. 
     * @param overwriteExisting if true, any existing file with same name will be overwritten on target
     * @return a generated ID for the file. Can be used to identify the file in progress listener. Especially useful when transmitting files with same names one after another. When Integer.MAX_VALUE is reached, ID is reset back to 0.
     */
    public int sendFile(File f, boolean overwriteExisting) {
        int id = sendId.getAndIncrement();
        for (FileSenderProgressListener listener : listeners) {
            listener.started(id, f, f.length());
        }
        sendPool.execute(new SendTask(f, id, overwriteExisting));
        return id;
    }

    /**
     * Adds a progress listener
     * @param listener progress listener implementation
     */
    public void addProgressListener(FileSenderProgressListener progressListener) {
        listeners.add(progressListener);
    }

    /**
     * Removes a progress listener
     * @param listener progress listener implementation
     */
    public void removeProgressListener(FileSenderProgressListener progressListener) {
        listeners.remove(progressListener);
    }

    /**
     * Ensure thread safety through always using a copy of listener list
     *
     * @return cloned listener list
     */
    private List<FileSenderProgressListener> getListenersCopy() {
        return new ArrayList<FileSenderProgressListener>(listeners);
    }
}
