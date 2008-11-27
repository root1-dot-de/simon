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

/**
 * Holds some static variables defining timeouts, thread and method names, ...
 * 
 * @author achr
 */
public class Statics {
	
	protected static final String TOSTRING_METHOD_SIGNATURE = "public java.lang.String java.lang.Object.toString()";
	protected static final String HASHCODE_METHOD_SIGNATURE = "public native int java.lang.Object.hashCode()";
	protected static final String EQUALS_METHOD_SIGNATURE 	= "public boolean java.lang.Object.equals(java.lang.Object)";

	protected static final int WAIT_FOR_SHUTDOWN_SLEEPTIME = 50;

	// values in seconds
	protected static int DEFAULT_IDLE_TIME = 10;
	protected static int DEFAULT_WRITE_TIMEOUT = 5;

	/** String that is needed for answering a "find server" packet */
	protected static final String REQUEST_STRING = "[SIMON|FindServer]";

	// some variables configured during ant build
	/** holds the version of SIMON */
	public static final String SIMON_VERSION = "@SIMON_VERSION@";
	/** holds the build time stamp of SIMON */
	public static final String SIMON_BUILD_TIMESTAMP = "@SIMON_BUILD_TIMESTAMP@";
	/** holds the revision number */
	public static final String SIMON_BUILD_REVISION = "@SIMON_BUILD_REVISION@";
	
	// some names for the used threads/pools
	protected static final String DISPATCHER_WORKERPOOL_NAME = "Simon.Dispatcher.WorkerPool";
	protected static final String PUBLISH_SERVICE_THREAD_NAME = "Simon.PublishService";
	protected static final String PUBLISH_CLIENT_THREAD_NAME = "Simon.PublishClient";
	
}
