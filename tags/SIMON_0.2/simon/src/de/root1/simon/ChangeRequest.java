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

import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.root1.simon.utils.Utils;

/**
 * A simple SIMON-internal plain object for holding changerequests on a {@link SocketChannel}
 * 
 * @author Alexander Christian
 */
public class ChangeRequest {
	
	protected transient Logger _log = Logger.getLogger(this.getClass().getName());
	
	/**
	 * ID for Register (value=1)
	 */
	public static final int REGISTER = 1;
	
	/**
	 * ID for ChangeOps (value=2)
	 */
	public static final int CHANGEOPS = 2;
	
	/**
	 * The SocketChannel which wants to change his interests
	 */
	public SocketChannel socket;
	
	/**
	 * the type of this change request. Should be either {@link ChangeRequest#REGISTER} of {@link ChangeRequest#CHANGEOPS}.
	 */
	public int type;
	
	/**
	 * The new OPs. 
	 */
	public int ops;
	
	
	/**
	 * 
	 * Creates a new "transport object" for a change request on a selector
	 * 
	 * @param socket the SocketChannel to which the change is related to
	 * @param type the type of the change (either {@link ChangeRequest#REGISTER} or {@link ChangeRequest#CHANGEOPS}
	 * @param ops the selection operation
	 */
	public ChangeRequest(SocketChannel socket, int type, int ops) {
		_log.finer("begin");
		this.socket = socket;
		this.type = type;
		this.ops = ops;
		if (_log.isLoggable(Level.FINEST))
			_log.finest(this.toString());
		_log.finer("end");
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[changerequest|");
		sb.append(Utils.getChannelIdentifier(socket));
		sb.append("type=");
		sb.append(getTypeAsString());
		sb.append(",ops=");
		sb.append(Utils.getOperationKeyAsString(ops));
		sb.append("]");
		return sb.toString();
	}

	/**
	 * 
	 * Returns a textual representation of the current set type
	 * 
	 * @return a string explaining the current set type
	 */
	private String getTypeAsString() {
		if (type==CHANGEOPS) return "CHANGEOPS";
		else
		if (type==REGISTER) return "REGISTER";
		else return "UNKNOWN_TYPE";
	}
}
