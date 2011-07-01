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
 * <code>INTERFACE_LOOKUP</code> message
 *
 * @author ACHR
 */
public class MsgInterfaceLookup extends AbstractMessage {
	
    private static final long serialVersionUID = 1L;

    private String canonicalInterfaceName;

    public MsgInterfaceLookup() {
    	super(SimonMessageConstants.MSG_INTERFACE_LOOKUP);
    }

    public String getCanonicalInterfaceName() {
        return canonicalInterfaceName;
    }

    public void setCanonicalInterfaceName(String canonicalInterfaceName) {
        this.canonicalInterfaceName = canonicalInterfaceName;
    }

    @Override
    public String toString() {
        // it is a good practice to create toString() method on message classes.
        return getSequence() + ":MsgInterfaceLookup(" + (canonicalInterfaceName.length()==0?"<NullLength>":canonicalInterfaceName) + ')';
    }
}
