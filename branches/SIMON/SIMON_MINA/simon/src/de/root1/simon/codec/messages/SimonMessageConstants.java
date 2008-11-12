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
package de.root1.simon.codec.messages;

/**
 * Provides Simon protocol constants.
 *
 * @author ACHR
 */
public final class SimonMessageConstants {
	
    public static final int TYPE_LEN = 2;
    public static final int SEQUENCE_LEN = 4;
    public static final int HEADER_LEN = TYPE_LEN + SEQUENCE_LEN;
    
    // ---------------------

	public static final int MSG_LOOKUP 				= 0;
	public static final int MSG_LOOKUP_RETURN 		= 1;
	
	public static final int MSG_INVOKE 				= 2;
	public static final int MSG_INVOKE_RETURN 		= 3;

	public static final int MSG_TOSTRING 			= 4;
	public static final int MSG_TOSTRING_RETURN 	= 5;

	public static final int MSG_EQUALS 				= 6;
	public static final int MSG_EQUALS_RETURN 		= 7;
	
	public static final int MSG_HASHCODE 			= 8;
	public static final int MSG_HASHCODE_RETURN 	= 9;

    private SimonMessageConstants() {
    }
}
