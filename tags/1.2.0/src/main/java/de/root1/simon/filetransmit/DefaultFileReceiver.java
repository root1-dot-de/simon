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

import de.root1.simon.RawChannelDataListener;
import de.root1.simon.Simon;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A default implementation to receive files from
 * <code>DefaultFileSender</code>. You can create your own by overwriting
 * methods, or by creating an own implementation which implemets
 * <code>FileReceiver</code>.
 *
 * @since 1.2.0
 * @author achristian
 */
@de.root1.simon.annotation.SimonRemote(value = {FileReceiver.class})
public class DefaultFileReceiver implements FileReceiver {

    /**
     * The logger used for this class
     */
    private final static Logger logger = LoggerFactory.getLogger(DefaultFileReceiver.class);
    public List<FileReceiverProgressListener> listeners = Collections.synchronizedList(new ArrayList<FileReceiverProgressListener>());
    private File downloadFolder;

    /**
     * RawChannelDataListener implementation which writes the file data to disk
     */
    private class ChannelDataListener implements RawChannelDataListener {

        private File f;
        private FileChannel fc;
        private final long length;
        private long bytesRead = 0;

        /**
         * Create a new data listener instance.
         * 
         * @param f the target file into which data is written
         * @param length the length of the target file. is used to verify amount of received data or update progress listener.
         * @param overwriteExisting overwrite existing files?
         * @throws FileNotFoundException if target file can not be opened
         * @throws IOException if file target file already exists
         */
        public ChannelDataListener(File f, long length, boolean overwriteExisting) throws FileNotFoundException, IOException {
            this.f = f;
            for (FileReceiverProgressListener listener : getListenersCopy()) {
                listener.started(f, length);
            }
            if (f.exists() && !overwriteExisting) {
                IOException e = new IOException("File " + f + " already exists");
                for (FileReceiverProgressListener listener : getListenersCopy()) {
                    listener.aborted(f, e);
                }
                throw e;
            } else {
                // try to delete upfront
                f.delete();
            }
            fc = new FileOutputStream(f).getChannel();
            this.length = length;
            logger.debug("Ready for receiving file {} with size {}", f.getAbsolutePath(), length);
        }

        @Override
        public void write(ByteBuffer data) {
            try {
                logger.trace("Receiving chunk ...");
                bytesRead += data.limit();

                // update progress listeners
                for (FileReceiverProgressListener listener : getListenersCopy()) {
                    listener.inProgress(f, bytesRead, length);
                }

                fc.write(data);
            } catch (IOException ex) {
                
                try {
                    logger.warn("Receiving of file {} aborted due to {}", f, ex);
                    for (FileReceiverProgressListener listener : getListenersCopy()) {
                        listener.aborted(f, ex);
                    }
                    fc.close();
                } catch (IOException ex1) {
                }
            }
        }

        @Override
        public void close() {
            if (fc.isOpen()) {
                
                try {
                    if (bytesRead!=length) {
                        String msg = "Received file "+f+" does not match expected file size. Found: "+bytesRead+". Expected: "+length;
                        for (FileReceiverProgressListener listener : getListenersCopy()) {
                            listener.aborted(f, new IOException(msg));
                        }
                        logger.warn(msg);
                    } else {
                        for (FileReceiverProgressListener listener : getListenersCopy()) {
                            listener.completed(f);
                        }
                    }
                    
                    
                    logger.debug("Receiving done");
                    fc.close();
                } catch (IOException ex) {
                    logger.warn("Problem closing received file "+f, ex);
                }
            }
        }
    }

    /**
     * Specify folder into which received files are stored.
     * 
     * @param folder download folder
     * @throws IllegalArgumentException if provided folder does not exist or is not a folder
     */
    public void setDownloadFolder(File folder) {
        if (!folder.exists()) {
            throw new IllegalArgumentException("Folder "+folder+" does not exist");
        }
        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("Folder "+folder+" is not a folder");
        }
        this.downloadFolder = folder;
    }

    /*
     * Remote method required by DefaultFileSender
     */
    @Override
    public int requestChannelToken(String name, long length, boolean overwriteExisting) throws FileNotFoundException, IOException {
        return Simon.prepareRawChannel(new ChannelDataListener(new File(downloadFolder, name), length, overwriteExisting), this);
    }

    /**
     * Adds a progress listener
     * @param listener progress listener implementation
     */
    public void addProgressListener(FileReceiverProgressListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a progress listener
     * @param listener progress listener implementation
     */
    public void removeProgressListener(FileReceiverProgressListener listener) {
        listeners.remove(listener);
    }

    /**
     * Ensure thread safety through always using a copy of listener list
     * 
     * @return cloned listener list
     */
    private List<FileReceiverProgressListener> getListenersCopy() {
        return new ArrayList<FileReceiverProgressListener>(listeners);
    }
}
