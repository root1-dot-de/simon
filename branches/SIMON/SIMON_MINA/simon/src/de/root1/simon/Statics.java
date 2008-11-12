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
 * TODO document me
 * @author achr
 *
 */
public class Statics {
	
	protected static final String TOSTRING_METHOD_SIGNATURE = "public java.lang.String java.lang.Object.toString()";
	protected static final String HASHCODE_METHOD_SIGNATURE = "public native int java.lang.Object.hashCode()";
	protected static final String EQUALS_METHOD_SIGNATURE 	= "public boolean java.lang.Object.equals(java.lang.Object)";
	public static final int DEFAULT_SOCKET_TIMEOUT = 100;

	protected static final int WAIT_FOR_SHUTDOWN_SLEEPTIME = 50;

	protected static int DGC_INTERVAL = 30000;

	/** String that is needed for answering a "find server" packet */
	public static final String REQUEST_STRING = "[SIMON|FindServer]";

	// some variables configured during ant build
	/** holds the version of SIMON */
	public static final String SIMON_VERSION = "@SIMON_VERSION@";
	/** holds the build timestamp of SIMON */
	public static final String SIMON_BUILD_TIMESTAMP = "@SIMON_BUILD_TIMESTAMP@";
	/** holds the versioning revision number */
	public static final String SIMON_BUILD_REVISION = "@SIMON_BUILD_REVISION@";
	
	// some names for the used threads/pools
	protected static final String CLIENT_DISPATCHER_THREAD_NAME = "Simon.Dispatcher{Client}";
	protected static final String SERVER_DISPATCHER_THREAD_NAME = "Simon.Dispatcher{Server}";
	protected static final String SERVER_ACCEPTOR_THREAD_NAME = "Simon.Registry.Acceptor";
	protected static final String SIMON_DGC_THREAD_NAME = "Simon.Dispatcher.DGC";
	protected static final String PINGWORKER_POOL_NAME = SIMON_DGC_THREAD_NAME+".PingWorkerPool";
	protected static final String DISPATCHER_WORKERPOOL_NAME = "Simon.Dispatcher.WorkerPool";
	protected static final String FILTERCHAIN_WORKERPOOL_NAME = "Simon.FILTERCHAIN.WorkerPool";
	public static final String PUBLISH_SERVICE_THREAD_NAME = "Simon.PublishService";
	public static final String PUBLISH_CLIENT_THREAD_NAME = "Simon.PublishClient";
	
}
