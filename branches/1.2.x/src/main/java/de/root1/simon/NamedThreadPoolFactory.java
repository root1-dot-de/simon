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
package de.root1.simon;

import java.util.concurrent.ThreadFactory;

/**
 * A factory-class that let's you use named threads in a thread-pool
 *
 * @author achristian
 *
 */
public class NamedThreadPoolFactory implements ThreadFactory {

	/** the base name for each thread created with this factory */
	private String baseName;
	
	private long i = 0;

	/**
	 * Creates a new thread-factory that gives each thread a basename
	 * 
	 * @param baseName the basename for the created threads
	 */
	protected NamedThreadPoolFactory(String baseName) {
		this.baseName = baseName;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
	 */
        @Override
	public Thread newThread(Runnable r) {
		if ((i++)==Long.MAX_VALUE) i=0;
                StringBuilder sb = new StringBuilder();
		sb.append(baseName);
		sb.append(".#");
		sb.append(i);
		return new ProcessMessageThread(r,sb.toString());
	}

}



