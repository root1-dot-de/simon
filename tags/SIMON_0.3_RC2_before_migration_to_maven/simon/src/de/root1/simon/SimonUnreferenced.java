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


/**
 * Implement this interface in you SimonRemote implementing object, and the unreferenced() method will be called if the network connection
 * related to the SimonRemote object is broken, terminated or closed. 
 * <br><br><b>Warning:</b><br>
 * <i>DO NOT CALL FURTHER REMOTE METHODS IN unreferenced(), AS THIS CAUSES FURTHER REMOTE-EXCEPTIONS!
 *  
 * @author achristian
 *
 */
public interface SimonUnreferenced {

	/**
	 * This method is called by SIMON if an instance of the object which is 
	 * implementing this interface, is no more referenced by a opposite endpoint
	 */
	public void unreferenced();

}
