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
package de.root1.simon.ssl;

import javax.net.ssl.SSLContext;

import de.root1.simon.Registry;

/**
 * This class is used by SIMON to get a {@link SSLContext} for the client and
 * server side, which is required by SSL powered communication.
 * 
 * @author Alexander Christian
 * @version 200901141248
 * 
 */
public interface SslContextFactory {

	/**
	 * Gets the {@link SSLContext} which is used by SIMON to create a SSL
	 * powered {@link Registry}
	 * 
	 * @return a ssl context object
	 */
	public abstract SSLContext getServerContext();

	/**
	 * Gets the {@link SSLContext} which is used by SIMON for looking up a SSL
	 * powered SIMON remote object
	 * 
	 * @return a ssl context object
	 */
	public abstract SSLContext getClientContext();

}