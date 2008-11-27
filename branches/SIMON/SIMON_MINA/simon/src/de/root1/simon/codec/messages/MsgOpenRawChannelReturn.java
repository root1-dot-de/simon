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
 * <code>OpenRawChannel RETURN</code> message 
 *
 * @author ACHR
 */
public class MsgOpenRawChannelReturn extends AbstractMessage {
	
    private static final long serialVersionUID = 1L;

    private boolean returnValue = false;

    public MsgOpenRawChannelReturn() {
    	super(SimonMessageConstants.MSG_OPEN_RAW_CHANNEL_RETURN);
    }

    public boolean getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(boolean returnValue) {
        this.returnValue = returnValue;
    }

    @Override
    public String toString() {
        // it is a good practice to create toString() method on message classes.
        return getSequence() + ":MsgOpenRawChannelReturn(" + returnValue + ')';
    }
}
