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
import java.util.logging.Level;
import java.util.logging.Logger;


public class SimonCallback implements Serializable {
	
	protected transient Logger _log = Logger.getLogger(this.getClass().getName());
	
	/**
	 * TODO: Documentation to be done for field 'serialVersionUID', by 'ACHR'..
	 *
	 */
	private static final long serialVersionUID = -1613858098177523543L;
	
	/**
	 * Name of the interface that is used to implement the callback-object
	 */
	private String interfaceName = null;
	private String id = null;
	
	

	/**
	 * 
	 * TODO: Documentation to be done for constructor 'SimonCallback', by 'ACHR'..
	 * 
	 * @param callback
	 */
	public SimonCallback(SimonRemote callback) {
		_log.fine("begin");
		this.id = callback.toString();

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
	 * TODO: Documentation to be done for method 'getInterfaceName', by 'ACHR'..
	 * 
	 * @return the callbacks interface
	 */
	public String getInterfaceName() {
		return interfaceName;
	}
	
	/**
	 * 
	 * TODO: Documentation to be done for method 'getInterfaceName', by 'ACHR'..
	 * 
	 * @return the callbacks ID
	 */
	public String getId() {
		return id;
	}
	


}
