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
	protected static final long DEFAULT_WAIT_TIME = 100;

	protected static int DGC_INTERVAL = 30000;
	
}
