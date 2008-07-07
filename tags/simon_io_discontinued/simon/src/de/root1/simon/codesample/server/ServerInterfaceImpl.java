/*
 * Copyright 2008 Alexander Christian <alex(at)root1.de>. All rights reserved.
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
package de.root1.simon.codesample.server;

import de.root1.simon.Simon;
import de.root1.simon.SimonRemoteException;
import de.root1.simon.codesample.common.ClientCallbackInterface;
import de.root1.simon.codesample.common.ServerInterface;

public class ServerInterfaceImpl implements ServerInterface {

	private static final long serialVersionUID = 1L;

	public void login(ClientCallbackInterface clientCallback) throws SimonRemoteException {
		clientCallback.callback("Dies ist der Callback. " +
				"Deine Adresse lautet "+Simon.getRemoteInetAddress(clientCallback)+" "+
				"und du bist verbunden auf dem lokalen Port "+Simon.getRemotePort(clientCallback));
	}

}
