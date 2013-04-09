/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.root1.simon;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 *
 * @author achristian
 */
public class RawChannelOutputStream extends OutputStream {
    
    private final RawChannel rawChannel;

    public RawChannelOutputStream(RawChannel rawChannel) {
        this.rawChannel = rawChannel;
    }

    @Override
    public void write(int b) throws IOException {
        rawChannel.write(ByteBuffer.wrap(new byte[]{(byte)b}));
    }

    @Override
    public void write(byte[] b) throws IOException {
        rawChannel.write(ByteBuffer.wrap(b));
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        rawChannel.write(ByteBuffer.wrap(b, off, len));
    }

    @Override
    public void flush() throws IOException {
        // there is no flush for RawChannel
    }

    @Override
    public void close() throws IOException {
        rawChannel.close();
    }
    
}
