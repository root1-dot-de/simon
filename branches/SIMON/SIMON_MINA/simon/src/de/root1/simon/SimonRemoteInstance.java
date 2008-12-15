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

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used by an endpoint if a remote object has to be "transferred" to the 
 * opposite endpoint. In such case, only the interface name is relevant. So an 
 * instance of this class is transferred instead of the "real" implementation of the remote object.
 * 
 * @author ACHR
 */
public class SimonRemoteInstance implements Serializable {
	
	private static final long serialVersionUID = 1;
	
	private transient final Logger logger = LoggerFactory.getLogger(getClass());
	
	/** Name of the interface that is used to implement the remote object */
	private String interfaceName = null;
	
	/** a unique identifier for the corresponding remote object */
	private String id = null;
	
	/** the remote object name of the simon proxy to which the SimonRemote belongs */
	private String remoteObjectName = null;
	

	/**
	 * 
	 * Creates a new SimonRemoteInstance transport object
	 * 
	 * @param session the {@link IoSession} to which the remote object is related to
	 * @param remoteObject the remote object for which we generate this transport object for
	 */
	protected SimonRemoteInstance(IoSession session, SimonRemote remoteObject) {
		logger.debug("begin");
		
		try {
			remoteObjectName = Simon.getSimonProxy(remoteObject).getRemoteObjectName();
		} catch (IllegalArgumentException e) {
			remoteObjectName="{SimonRemoteInstance:RemoteObjectNameNotAvailable}";
		}
		
		
		String IP = session.getRemoteAddress().toString();
		long sessionId = session.getId();
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("[");
		sb.append(remoteObject.getClass().getName());
		sb.append("|ip=");
		sb.append(IP);
		sb.append(";sessionID=");
		sb.append(sessionId);
		sb.append(";remoteObjectHash=");
		sb.append(remoteObject.hashCode());
		sb.append("]");
		
		this.id = sb.toString();
		
		logger.debug("SimonRemoteInstance created with id={}", this.id);
		
		// get the interfaces the arg has implemented
		Class<?>[] remoteObjectInterfaceClasses = remoteObject.getClass().getInterfaces();

		// check each interface if THIS is the one which implements "SimonRemote"
		for (Class<?> remoteObjectInterfaceClass : remoteObjectInterfaceClasses) {
			

			String remoteObjectInterfaceClassNameTemp = remoteObjectInterfaceClass.getName();
			
			logger.debug("Checking interfacename='{}' for '{}'", remoteObjectInterfaceClassNameTemp, SimonRemote.class.getName());
			
			// Get the interfaces of the implementing interface
			Class<?>[] remoteObjectInterfaceSubInterfaces = remoteObjectInterfaceClass.getInterfaces();
			
			boolean isSimonRemote = false;
			for (Class<?> remoteObjectInterfaceSubInterface : remoteObjectInterfaceSubInterfaces) {
				
				logger.debug("Checking child interfaces for '{}': child={}", remoteObjectInterfaceClassNameTemp, remoteObjectInterfaceSubInterface);
				
				if (remoteObjectInterfaceSubInterface.getName().equalsIgnoreCase(SimonRemote.class.getName())) {
					isSimonRemote = true;
					break;
				}
			}
			
			if (isSimonRemote){
				interfaceName = remoteObjectInterfaceClassNameTemp;
				
				logger.debug("SimonRemote found in arg: interfaceName='{}'", interfaceName);
				
				break;

			} else {
				interfaceName = null;
			}
		}
		logger.debug("end");
	}

	/**
	 * 
	 * Returns the name of the interface of the remote object's implementation
	 * 
	 * @return the remote object's interface
	 */
	protected String getInterfaceName() {
		return interfaceName;
	}
	
	/**
	 * 
	 * Returns an unique identifier for this remote object. This is necessary to differ from two 
	 * remote objects with the same implementation
	 * 
	 * @return a unique ID for the remote object
	 */
	protected String getId() {
		return id;
	}
	
	/**
	 * Returns the proxy's remote object name in the related lookup table
	 * @return the remote object name
	 */
	protected String getRemoteObjectName() {
		return remoteObjectName;
	}
	
}
