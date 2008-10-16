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

import java.nio.channels.SelectionKey;

import org.apache.mina.core.session.IoSession;

/**
 * 
 * This class is used to store the relation between:
 * 
 * <ul>
 * <li>{@link Dispatcher}</li>
 * <li>{@link SelectionKey}</li>
 * <li>a String of format "HOST:PORT" that identifies the server</li>
 * </ul>
 * 
 * This information is used by {@link Simon} to establish only one connection to a server for
 * several {@link Simon#lookup} calls. Each lookup on the same connection increases a reference count. 
 * The connection will be dropped by SIMON if the reference count reaches 0.
 * 
 * @author ACHR
 */
public class ClientToServerConnection {
	
	private Dispatcher dispatcher;
	private IoSession session;
	private String ServerString;
	private int referenceCount = 0;

	/**
	 * Creates a new Instance of {@link ClientToServerConnection}
	 * 
	 * @param serverString the used server string
	 * @param dispatcher the used dispatcher
	 * @param key the used key
	 */
	public ClientToServerConnection(String serverString,
			Dispatcher dispatcher, IoSession session) {
		
		this.ServerString = serverString;
		this.dispatcher = dispatcher;
		this.session = session;
	}
	
	/**
	 * 
	 * Increases the reference count by one
	 * 
	 * @return the new reference count
	 */
	public int addRef() {
		return ++referenceCount;
	}
	
	/**
	 * Decreases the reference count by one
	 * 
	 * @return the new reference count
	 */
	public int delRef(){
		return --referenceCount;
	}

	/**
	 * Gets the {@link Dispatcher} the client uses to cummunicate with the network
	 *
	 */
	public Dispatcher getDispatcher() {
		return dispatcher;
	}
	
	/**
	 * Sets the {@link Dispatcher} the client uses to cummunicate with the network
	 *
	 */
	public void setDispatcher(Dispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}
	
	/**
	 * Gets the session which is used by the client to communicate with the server via {@link Dispatcher}
	 *
	 */
	
	public IoSession getSession() {
		return session;
	}
	
	/**
	 * Sets the session which is used by the client to communicate with the server via {@link Dispatcher}
	 *
	 */
	public void setSession(IoSession session) {
		this.session = session;
	}
	
	/**
	 * Gets the server string
	 *
	 */
	public String getServerString() {
		return ServerString;
	}
	
	/**
	 * Sets the server string
	 *
	 */
	public void setServerString(String serverString) {
		ServerString = serverString;
	}

}
