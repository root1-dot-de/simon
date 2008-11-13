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

import java.util.concurrent.ExecutorService;

import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * This class is used to store the relation between:
 * 
 * <ul>
 * <li>{@link Dispatcher}</li>
 * <li>{@link IoSession}</li>
 * <li>{@link IoConnector}</li>
 * <li>a String of format "HOST:PORT" that identifies the server</li>
 * <li>and the {@link ExecutorService} used on the filter chain</li>
 * </ul>
 * 
 * This information is used by {@link Simon} to establish only one connection to a server for
 * several {@link Simon#lookup} calls. Each lookup on the same connection increases a reference count. 
 * The connection will be dropped by SIMON if the reference count reaches 0.
 * 
 * @author ACHR
 */
public class ClientToServerConnection {
	
	@SuppressWarnings("unused")
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private Dispatcher dispatcher;
	private IoSession session;
	private String ServerString;
	private int referenceCount = 0;
	private IoConnector connector;
	private ExecutorService filterchainWorkerPool;

	/**
	 * Returns the {@link IoConnector} related to the connection
	 * @return the related {@link IoConnector}
	 */
	public IoConnector getConnector() {
		return connector;
	}

	/**
	 * TODO document me
	 * @param connector
	 */
	public void setConnector(NioSocketConnector connector) {
		this.connector = connector;
	}

	/**
	 * Creates a new Instance of {@link ClientToServerConnection}
	 * 
	 * @param serverString the used server string
	 * @param dispatcher the used dispatcher
	 * @param session
	 * @param connector 
	 * @param filterchainWorkerPool 
	 */
	public ClientToServerConnection(String serverString,
			Dispatcher dispatcher, IoSession session, IoConnector connector, ExecutorService filterchainWorkerPool) {
		
		this.ServerString = serverString;
		this.dispatcher = dispatcher;
		this.session = session;
		this.connector = connector;
		this.filterchainWorkerPool = filterchainWorkerPool;
	}
	
	public ExecutorService getFilterchainWorkerPool() {
		return filterchainWorkerPool;
	}

	public void setFilterchainWorkerPool(ExecutorService filterchainWorkerPool) {
		this.filterchainWorkerPool = filterchainWorkerPool;
	}

	/**
	 * 
	 * Increases the reference count by one
	 * 
	 * @return the new reference count
	 */
	public synchronized int addRef() {
		return ++referenceCount;
	}
	
	/**
	 * Decreases the reference count by one
	 * 
	 * @return the new reference count
	 */
	public synchronized int delRef(){
		return --referenceCount;
	}
	
	/**
	 * Returns the current valid reference count
	 * @return the current reference count
	 */
	public int getRefCount(){
		return referenceCount;
	}

	/**
	 * Gets the {@link Dispatcher} the client uses to communicate with the network
	 * @return the stored dispatcher
	 */
	public Dispatcher getDispatcher() {
		return dispatcher;
	}
	
	/**
	 * Sets the {@link Dispatcher} the client uses to communicate with the network
	 * @param dispatcher the dispatcher to store
	 */
	public void setDispatcher(Dispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}
	
	/**
	 * Gets the session which is used by the client to communicate with the server via {@link Dispatcher}
	 * @return the stored session
	 */
	public IoSession getSession() {
		return session;
	}
	
	/**
	 * Sets the session which is used by the client to communicate with the server via {@link Dispatcher}
	 * @param session the session to store
	 */
	public void setSession(IoSession session) {
		this.session = session;
	}
	
	/**
	 * Gets the server string
	 * @return the stored server string
	 */
	public String getServerString() {
		return ServerString;
	}
	
	/**
	 * Sets the server string
	 * @param serverString the server string to store
	 */
	public void setServerString(String serverString) {
		ServerString = serverString;
	}

}
