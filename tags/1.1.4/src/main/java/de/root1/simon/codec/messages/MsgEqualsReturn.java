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
 * <code>Equals RETURN</code> message 
 *
 * @author ACHR
 */
public class MsgEqualsReturn extends AbstractMessage {
    private static final long serialVersionUID = 1L;

    private boolean equalsResult;

    public MsgEqualsReturn() {
    	super(SimonMessageConstants.MSG_EQUALS_RETURN);
    }

    public boolean getEqualsResult() {
		return equalsResult;
	}

	public void setEqualsResult(boolean equalsResult) {
		this.equalsResult = equalsResult;
	}

	@Override
    public String toString() {
        // it is a good practice to create toString() method on message classes.
        return getSequence() + ":MsgEqualsReturn(" + equalsResult + ')';
    }
}
