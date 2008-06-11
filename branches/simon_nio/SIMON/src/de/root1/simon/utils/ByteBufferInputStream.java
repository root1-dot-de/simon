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
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ByteBufferInputStream extends InputStream {

	
	private ByteBuffer bb;
	
	public ByteBufferInputStream(ByteBuffer bb) {
		this.bb = bb;
	}
	
	public synchronized int read() {
		return bb.get();
	}
	
	public int read(byte[] b) throws IOException {
		bb.get(b);
		return b.length;
	}
	
	public synchronized int read(byte[] b, int off, int len) {
		bb.get(b, off, len);
		return len;
	}
	
	public synchronized int available() {
		return bb.limit()-bb.position();
	}

	public boolean markSupported() {
		return false;
	}
	
	public synchronized void reset() {
	}
	
	public void mark(int readAheadLimit) {
	}
	
	public synchronized long skip(long n) {
		bb.position((int)(bb.position()+n));
		return n;
	}
	
	
	
	
	
	

}
