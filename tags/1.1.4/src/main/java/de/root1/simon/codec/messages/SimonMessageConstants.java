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
    public static final int BODYSIZE_LEN = 4;
    public static final int HEADER_LEN = TYPE_LEN + SEQUENCE_LEN + BODYSIZE_LEN;
    
    // ---------------------

    public static final byte MSG_NAME_LOOKUP = 0x00;
    public static final byte MSG_NAME_LOOKUP_RETURN = 0x01;

    public static final byte MSG_INVOKE = 0x02;
    public static final byte MSG_INVOKE_RETURN = 0x03;

    public static final byte MSG_TOSTRING = 0x04;
    public static final byte MSG_TOSTRING_RETURN = 0x05;

    public static final byte MSG_EQUALS = 0x06;
    public static final byte MSG_EQUALS_RETURN = 0x07;

    public static final byte MSG_HASHCODE = 0x08;
    public static final byte MSG_HASHCODE_RETURN = 0x09;

    public static final byte MSG_OPEN_RAW_CHANNEL = 0x0A;
    public static final byte MSG_OPEN_RAW_CHANNEL_RETURN = 0x0B;

    public static final byte MSG_CLOSE_RAW_CHANNEL = 0x0C;
    public static final byte MSG_CLOSE_RAW_CHANNEL_RETURN = 0x0D;

    public static final byte MSG_RAW_CHANNEL_DATA = 0x0E;
    public static final byte MSG_RAW_CHANNEL_DATA_RETURN = 0x0F;

    public static final byte MSG_PING = 0x10;
    public static final byte MSG_PONG = 0x11;

    public static final byte MSG_INTERFACE_LOOKUP = 0x12;
    public static final byte MSG_INTERFACE_LOOKUP_RETURN = 0x13;
    
    public static final byte MSG_ERROR = 0x14;
    
    public static final byte MSG_RELEASE_REF = 0x15;
	
    private SimonMessageConstants() {
    }
}
