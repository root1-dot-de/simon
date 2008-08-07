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

import java.io.Serializable;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is used by an endpoint if a callback object has to be "transferred" to the 
 * opposite endpoint. In such case, only the interfacename is relevant. So we transfer an 
 * instance of this class instead of the "real" implementation of the callback object.
 * 
 * @author ACHR
 */
public class SimonCallback implements Serializable {
	
	protected transient Logger _log = Logger.getLogger(this.getClass().getName());
	
	private static final long serialVersionUID = 1;
	
	/** Name of the interface that is used to implement the callback-object */
	private String interfaceName = null;
	
	/** a unique identifier for the corresponding callback object */
	private String id = null;
	

	/**
	 * 
	 * Creates a new SimonCallback transport object
	 * 
	 * @param key the key to which the callback is related to
	 * @param callback the callback we generate this transport object for
	 */
	public SimonCallback(SelectionKey key, SimonRemote callback) {
		_log.fine("begin");
		
		SocketChannel socketChannel = (SocketChannel) key.channel();
		String IP = socketChannel.socket().getInetAddress().getHostAddress();
		int remotePort = socketChannel.socket().getPort();
		int localPort = socketChannel.socket().getLocalPort();
		
		this.id = "["+callback.getClass().getName()+"|ip="+IP+";l_port="+localPort+";r_port="+remotePort+";remoteObjectHash="+callback.hashCode()+"]";
		
		if (_log.isLoggable(Level.FINER)){
			_log.finer("callbackId="+this.id);
		}

		// get the interfaces the arg has implemented
		Class<?>[] callbackInterfaceClasses = callback.getClass().getInterfaces();

		// check each interface if THIS is the one which implements "SimonRemote"
		for (Class<?> callbackInterfaceClass : callbackInterfaceClasses) {
			

			String callbackInterfaceClassNameTemp = callbackInterfaceClass.getName();
			if (_log.isLoggable(Level.FINER))
				_log.finer("Checking interfacename='"+callbackInterfaceClassNameTemp+"' for '"+SimonRemote.class.getName()+"'");
			
			// Get the interfaces of the implementing interface
			Class<?>[] callbackInterfaceSubInterfaces = callbackInterfaceClass.getInterfaces();
			
			boolean isSimonRemote = false;
			for (Class<?> callbackInterfaceSubInterface : callbackInterfaceSubInterfaces) {
				if (_log.isLoggable(Level.FINER))
					_log.finer("Checking child interfaces for '"+callbackInterfaceClassNameTemp+"': child="+callbackInterfaceSubInterface);
				if (callbackInterfaceSubInterface.getName().equalsIgnoreCase(SimonRemote.class.getName())) {
					isSimonRemote = true;
					break;
				}
			}
			
			if (isSimonRemote){
				interfaceName = callbackInterfaceClassNameTemp;
				if (_log.isLoggable(Level.FINER))
					_log.finer("SimonRemote found in arg: interfaceName='"+interfaceName+"'");
				break;

			} else {
				interfaceName = null;
			}
		}
		_log.fine("end");
	}

	/**
	 * 
	 * Returns the name of the interface of the callbackobjects implementation
	 * 
	 * @return the callbacks interface
	 */
	public String getInterfaceName() {
		return interfaceName;
	}
	
	/**
	 * 
	 * Returns an unique identifier for this callabck-object. This is neccessary to differ from two 
	 * callback object with the same implementation
	 * 
	 * @return the callbacks ID
	 */
	public String getId() {
		return id;
	}
	
}
