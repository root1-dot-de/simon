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
package de.root1.simon.exceptions;

/**
 * 
 * Exception which normally occurs if the type of the received packet is unknown.<br>
 * This also means that there can/could be a communication problem.
 * 
 * @author ACHR
 */
public class InvalidPacketTypeException extends SimonRemoteException {
	
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * Creates a new exception with a exception message 
	 * 
	 * @param msg
	 */
	public InvalidPacketTypeException(String msg)  {
		super(msg);
	}

}
