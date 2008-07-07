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
package de.root1.simon.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class ByteBufferOutputStream extends OutputStream {

	
	ByteBuffer bb = ByteBuffer.allocate(4096);
	
	public void skipBytes(int n){
		bb.position(bb.position()+n);
	}
	
	@Override
	public void write(int b) throws IOException {

		if (bb.position()==bb.limit())
			bb = Utils.doubleByteBuffer(bb);
		
		bb.put((byte)b);
		
	}
	
	@Override
	public void write(byte[] b) throws IOException {
		bb.put(b);
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		bb.put(b, off, len);
	}
	
	public ByteBuffer getByteBuffer(){
		bb.flip();
		return bb;
	}
	
	public int getPosition(){
		return bb.position();
	}

	
    /**
     * Returns the current size of the buffer.
     *
     * @return  the value of the <code>count</code> field, which is the number
     *          of valid bytes in this output stream.
     * @see     java.io.ByteArrayOutputStream#count
     */
    public synchronized int size() {
    	return bb.capacity();
    }

}
