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
 *
 * A SIMON remote object implementation should implement the SimonUnreferenced
 * interface to receive notification when there are no more clients that 
 * reference that remote object.
 * <br>
 * <br><b>Warning:</b><br>
 * <i>DO NOT CALL ANY REMOTE METHOD IN unreferenced(), AS THIS WILL CAUSE 
 * SIMONREMOTEEXCEPTIONS!</i>
 *  
 * @author achristian
 *
 */
public interface SimonUnreferenced {

    /**
     * Called by the SIMON dispatcher sometime after the dispatcher determines
     * that the reference list, the list of clients referencing the remote
     * object, becomes empty. 
     */
    public void unreferenced();
}
