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
 * This exception is thrown, if the connection to SIMONs opposite is broken 
 * (IOException or things like that)
 * 
 * @author ACHR
 */
public class ConnectionException extends SimonRemoteException {
	
	private static final long serialVersionUID = 2999458111410425350L;

	public ConnectionException(String msg){
		super(msg);
	}

}
