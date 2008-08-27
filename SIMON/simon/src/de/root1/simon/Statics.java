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

public class Statics {
	
	public static final byte	SIMON_PACKET_HEADER_ID0		= 0x23;
	public static final byte	SIMON_PACKET_HEADER_ID1		= 0x32;
	
	public static final byte 	LOOKUP_PACKET 				= 0x00;
	public static final byte 	LOOKUP_RETURN_PACKET 		= 0x01;
	public static final byte 	INVOCATION_PACKET 			= 0x02;
	public static final byte 	INVOCATION_RETURN_PACKET 	= 0x03;
	public static final byte 	TOSTRING_PACKET			 	= 0x04;
	public static final byte 	TOSTRING_RETURN_PACKET	 	= 0x05;
	public static final byte 	HASHCODE_PACKET			 	= 0x06;
	public static final byte 	HASHCODE_RETURN_PACKET	 	= 0x07;
	public static final byte 	EQUALS_PACKET			 	= 0x08;
	public static final byte 	EQUALS_RETURN_PACKET	 	= 0x09;
	public static final byte 	PING_PACKET	 				= 0x0A;
	public static final byte 	PONG_PACKET	 				= 0x0B;
	
	protected static final String TOSTRING_METHOD_SIGNATURE = "public java.lang.String java.lang.Object.toString()";
	protected static final String HASHCODE_METHOD_SIGNATURE = "public native int java.lang.Object.hashCode()";
	protected static final String EQUALS_METHOD_SIGNATURE 	= "public boolean java.lang.Object.equals(java.lang.Object)";
	public static final int DEFAULT_SOCKET_TIMEOUT = 100;

	protected static int DGC_INTERVAL = 30000;

	/**String that is needed for answering a "find server" packet */
	public static final String REQUEST_STRING = "[SIMON|FindServer]";

	// some variables configured during ant build
	/** holds the version of SIMON */
	public static final String SIMON_VERSION = "@SIMON_VERSION@";
	/** holds the build timestamp of SIMON */
	public static final String SIMON_BUILD_TIMESTAMP = "@SIMON_BUILD_TIMESTAMP@";
	/** holds the versioning revision number */
	public static final String SIMON_BUILD_REVISION = "@SIMON_BUILD_REVISION@";
	
	// some names for the used threads/pools
	protected static final String CLIENT_DISPATCHER_THREAD_NAME = "Simon.Dispatcher";
	protected static final String SERVER_DISPATCHER_THREAD_NAME = "Simon.Registry.Dispatcher";
	protected static final String SERVER_ACCEPTOR_THREAD_NAME = "Simon.Registry.Acceptor";
	protected static final String SIMON_DGC_THREAD_NAME = "Simon._Dispatcher.DGC";
	protected static final String PINGWORKER_POOL_NAME = SIMON_DGC_THREAD_NAME+".PingWorkerPool";
	protected static final String DISPATCHER_WORKERPOOL_NAME = "Simon._Dispatcher.WorkerPool";
	
}