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
 * General exception used by SIMON. All other "not from SUN" exceptions should extend this class.
 * 
 * @author ACHR
 */
public class SimonRemoteException extends RuntimeException {

    private static final long serialVersionUID = -4586636119330576150L;

    /**
     * 
     * Creates a new exception with e exception message
     * 
     * @param msg a exception message
     */
    public SimonRemoteException(String msg) {
        super(msg);
    }

    /**
     * Creates a new exception with e exception message and a root-cause
     * @param message
     * @param cause 
     */
    public SimonRemoteException(String message, Throwable cause) {
        super(message, cause);
    }
}
