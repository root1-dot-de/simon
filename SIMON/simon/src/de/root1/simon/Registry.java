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

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

import de.root1.simon.exceptions.LookupFailedException;
import de.root1.simon.exceptions.NameBindingException;

/**
 * The SIMON server acts as a registry for remote objects. 
 * So, Registry is SIMON's internal server implementation
 *
 * @author achristian
 *
 */
public class Registry {
	
	protected transient Logger _log = Logger.getLogger(this.getClass().getName());
	
	private LookupTable lookupTableServer;
	private InetAddress address;
	private int port;

	private Dispatcher dispatcher;
	private Acceptor acceptor;
	
	/** The pool in which the dispatcher, acceptor and registry lives */
	private ExecutorService threadPool;

//	/**
//	 * Creates a registry with a reference to a given {@link LookupTable}.
//	 * This is used by the main class {@link Simon} if one uses a global registry.
//	 *  
//	 * @param lookupTable a reference to an existing {@link LookupTable}
//	 * @param port the port the registry listens on for new connections
//	 * @param threadPool a reference to an existing thread pool
//	 * @throws UnknownHostException 
//	 */
//	public Registry(LookupTable lookupTable, int port, ExecutorService threadPool) throws UnknownHostException {
//		_log.fine("begin");
//		this.lookupTableServer = lookupTable;
//		this.address = InetAddress.getByName("0.0.0.0");
//		this.port = port;
//		this.threadPool = threadPool;
//		_log.fine("end");
//	}
	
	/**
	 * Creates a registry which has it's own {@link LookupTable} instead of a global.
	 *  
	 * @param port the port the registry listens on for new connections
	 * @param threadPool a reference to an existing thread pool
	 * @throws IOException 
	 */
	public Registry(InetAddress address, int port, ExecutorService threadPool) throws IOException {
		_log.fine("begin");
		this.lookupTableServer = new LookupTable();
		this.address  = address;
		this.port = port;
		this.threadPool = threadPool;
		start();
		_log.fine("end");
	}

	/**
	 * Starts the registry thread
	 * @throws IOException if there's a problem getting a selector for the non-blocking network communication, or if the 
	 *
	 */
	private void start() throws IOException {

		_log.fine("begin");
		
			
		dispatcher = new Dispatcher(null, lookupTableServer, threadPool);
		new Thread(dispatcher,Statics.SERVER_DISPATCHER_THREAD_NAME).start();
		_log.finer("dispatcher thread created and started");
		
		acceptor = new Acceptor(address, dispatcher,port);
		new Thread(acceptor,Statics.SERVER_ACCEPTOR_THREAD_NAME).start();
		_log.finer("acceptor thread created and started");			
			
		
		_log.fine("end");
	}
	
	/**
	 * Stops the registry. This clears the {@link LookupTable}, 
	 * stops the {@link Acceptor} and the {@link Dispatcher}.
	 * After running this method, no further connection/communication is possible with this 
	 * registry.
	 *
	 */
	protected void stop() {
		lookupTableServer.clear();
		acceptor.shutdown();
		dispatcher.shutdown();
		Simon.removeRegistryFromList(this);
	}
	
	/**
	 * Binds a remote object to the registry's own {@link LookupTable}
	 * 
	 * @param name a name for object to bind
	 * @param remoteObject the object to bind
	 * @throws NameBindingException if there are problems binding the remoteobject to the registry
	 */
	public void bind(String name, SimonRemote remoteObject) throws NameBindingException {
		try {
			if (lookupTableServer.getRemoteBinding(name)!=null) 
				throw new NameBindingException("a remote object with the name '"+name+"' is already bound to this registry. unbind() first, or alternatively rebind().");
		} catch (LookupFailedException e) {
			// nothing to do
		}
		lookupTableServer.putRemoteBinding(name, remoteObject);
	}
	
	/**
	 * Unbinds a remote object from the registry's own {@link LookupTable}
	 *  
	 * @param name the object to unbind
	 */
	public void unbind(String name){
		//TODO what to do with already connected users?
		lookupTableServer.releaseRemoteBinding(name);
	}
	
	/**
	 * As the name says, it re-binds a remote object.
	 * This method shows the same behavior as the following two commands in sequence:<br>
	 * <br>
	 * <code>
	 * unbind(name);<br>
	 * bind(name, remoteObject);
	 * </code>
	 * @param name the name of the object to rebind
	 * @param remoteObject the object to rebind
	 */
	public void rebind(String name, SimonRemote remoteObject){
		unbind(name);
		try {
			bind(name, remoteObject);
		} catch (NameBindingException e) {
			// this should never happen, nevertheless, we log it
			_log.warning("rebind() should never throw an NameBindingException. Contact SIMON author and send him this log.");
		}
	}
	
	/**
	 * Returns whether the registry is running and active or not
	 * @return boolean
	 */
	public boolean isRunning(){
		return (dispatcher.isRunning() || acceptor.isRunning());
	}
	
}
