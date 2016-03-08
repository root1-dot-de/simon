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
package de.root1.simon;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;

/**
 * InputStream wrapper for SIMON RawChannel
 *
 * @author achristian
 * @since 1.2.0
 */
public class RawChannelInputStream extends InputStream {

    private RawChannelDataListener dataListener = new RawChannelDataListener() {
        @Override
        public void write(ByteBuffer data) {
            int len = data.limit();
            data.get(buf, 0, len);
            try {
                pos.write(buf, 0, len);
            } catch (IOException ex) {
                exception = ex;
            }
        }

        @Override
        public void close() {
            try {
                pos.close();
            } catch (IOException ex) {
                exception = ex;
            }
        }
    };
    private IOException exception;
    private PipedInputStream pis;
    private PipedOutputStream pos;
    private final byte[] buf;
    /**
     * Default block size of 8k
     */
    public static final int DEFAULT_BLOCKSIZE = 8 * 1024;

    /**
     * Creates a new input stream. You need to connect it to SIMON via
     * {@link Simon#prepareRawChannel(de.root1.simon.RawChannelDataListener, java.lang.Object)}.
     * Pass the output of
     * {@link RawChannelInputStream#getRawChannelDataListener()} as
     * {@link RawChannelDataListener}.
     *
     * @throws IOException
     */
    public RawChannelInputStream() throws IOException {
        this(DEFAULT_BLOCKSIZE);
    }

    /**
     * @see RawChannelInputStream#RawChannelInputStream() 
     * @param blockSize the block-size for reading from rawchannel and wriuting to the inputstream pipe
     * @throws IOException 
     */
    public RawChannelInputStream(int blockSize) throws IOException {
        buf = new byte[blockSize];
        pos = new PipedOutputStream();
        pis = new PipedInputStream(pos, blockSize);
    }

    @Override
    public int read() throws IOException {
        if (exception != null) {
            throw new IOException("Error occured while forwarding data from RawChannelDataListener to InputStream", exception);
        }
        return pis.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        if (exception != null) {
            throw new IOException("Error occured while forwarding data from RawChannelDataListener to InputStream", exception);
        }
        return pis.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (exception != null) {
            throw new IOException("Error occured while forwarding data from RawChannelDataListener to InputStream", exception);
        }
        return pis.read(b, off, len);
    }

    @Override
    public synchronized void reset() throws IOException {
        if (exception != null) {
            throw new IOException("Error occured while forwarding data from RawChannelDataListener to InputStream", exception);
        }
        pis.reset();
    }

    @Override
    public int available() throws IOException {
        if (exception != null) {
            throw new IOException("Error occured while forwarding data from RawChannelDataListener to InputStream", exception);
        }
        return pis.available();
    }

    @Override
    public void close() throws IOException {
        if (exception != null) {
            throw new IOException("Error occured while forwarding data from RawChannelDataListener to InputStream", exception);
        }
        pos.close();
        pis.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        pis.mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        return pis.markSupported();
    }

    @Override
    public long skip(long n) throws IOException {
        if (exception != null) {
            throw new IOException("Error occured while forwarding data from RawChannelDataListener to InputStream", exception);
        }
        return pis.skip(n);
    }

    /**
     * Get the underlying SIMON RawChannelDataListener implementation for this
     * input stream.
     *
     * @return RawChannelDataListener implementation
     */
    public RawChannelDataListener getRawChannelDataListener() {
        return dataListener;
    }
}
