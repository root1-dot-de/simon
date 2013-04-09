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
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * OutputStream wrapper for SIMON RawChannel
 * @author achristian
 * @since 1.2.0
 */
public class RawChannelOutputStream extends OutputStream {
    
    private final RawChannel rawChannel;

    /**
     * To create an outputstream for a rawchannel, you actually need an raw channel
     * @param rawChannel the raw channel to write to
     */
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