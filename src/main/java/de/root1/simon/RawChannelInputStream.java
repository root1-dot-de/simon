/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.root1.simon;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;

/**
 *
 * @author achristian
 */
public class RawChannelInputStream extends InputStream {

    private RawChannelDataListener dataListener = new RawChannelDataListener() {

        @Override
        public void write(ByteBuffer data) {
            int len = data.limit();
            data.get(buf,0,len);
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

    public RawChannelInputStream() throws IOException {
        this(DEFAULT_BLOCKSIZE);
    }

    public RawChannelInputStream(int blockSize) throws IOException {
        buf = new byte[blockSize];
        pos = new PipedOutputStream();
        pis = new PipedInputStream(pos, blockSize);
    }
    
    @Override
    public int read() throws IOException {
        if (exception!=null) {
            throw new IOException("Error occured while forwarding data from RawChannelDataListener to InputStream", exception);
        }
        return pis.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        if (exception!=null) {
            throw new IOException("Error occured while forwarding data from RawChannelDataListener to InputStream", exception);
        }
        return pis.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (exception!=null) {
            throw new IOException("Error occured while forwarding data from RawChannelDataListener to InputStream", exception);
        }
        return pis.read(b, off, len);
    }

    @Override
    public synchronized void reset() throws IOException {
        if (exception!=null) {
            throw new IOException("Error occured while forwarding data from RawChannelDataListener to InputStream", exception);
        }
        pis.reset();
    }

    @Override
    public int available() throws IOException {
        if (exception!=null) {
            throw new IOException("Error occured while forwarding data from RawChannelDataListener to InputStream", exception);
        }
        return pis.available();
    }

    @Override
    public void close() throws IOException {
        if (exception!=null) {
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
        if (exception!=null) {
            throw new IOException("Error occured while forwarding data from RawChannelDataListener to InputStream", exception);
        }
        return pis.skip(n);
    }

    /**
     * Get the underlying SIMON RawChannelDataListener implementation for this input stream.
     * 
     * @return RawChannelDataListener implementation
     */
    public RawChannelDataListener getRawChannelDataListener() {
        return dataListener;
    }
    
}
