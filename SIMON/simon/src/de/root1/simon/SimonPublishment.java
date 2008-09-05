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

import java.net.InetAddress;

/**
 * TODO document me
 * @author ACHR
 *
 */
public class SimonPublishment {
	
	private static final String SIMON_PUBLISHMENT_HEADER = "[SIMON|";
	private String remoteObjectName;
	private int port;
	private InetAddress address;
	StringBuffer sb = new StringBuffer();

	public SimonPublishment(InetAddress address, int port, String remoteObjectName) {
		this.address = address;
		this.port = port;
		this.remoteObjectName = remoteObjectName;
	}
	
	public SimonPublishment(String rawString) throws IllegalArgumentException {
		if (!rawString.substring(0, SIMON_PUBLISHMENT_HEADER.length()).equals(SIMON_PUBLISHMENT_HEADER) ) 
			throw new IllegalArgumentException("provided raw string has the wrong format: "+rawString);
	}
	
	public String getRemoteObjectName() {
		return remoteObjectName;
	}
	
	public int getPort() {
		return port;
	}
	
	public InetAddress getAddress() {
		return address;
	}
	
	@Override
	public String toString() {
		sb.append(SIMON_PUBLISHMENT_HEADER);
		sb.append(address.getHostAddress());
		sb.append(":");
		sb.append(port);
		sb.append("|");
		sb.append(remoteObjectName);
		sb.append("]");
		return sb.toString();
	}
	

}
