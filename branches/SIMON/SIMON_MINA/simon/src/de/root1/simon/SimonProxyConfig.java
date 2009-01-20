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
 * TODO document me
 * @author Alexander Christian
 * @version 200901201553
 *
 */
public class SimonProxyConfig {
	
	/**
	 * The host of the proxy server
	 */
	private String proxyHost;
	/**
	 * The port on which the proxy server listens
	 */
	private int proxyPort;
	/**
	 * Does the proxy require authentication?
	 */
	private boolean authRequired = false;
	/**
	 * username if authentication is required
	 */
	private String username;
	/**
	 * Password if authentication is required
	 */
	private String password;
	/**
	 * @return the proxyHost
	 */
	public String getProxyHost() {
		return proxyHost;
	}
	/**
	 * @param proxyHost the proxyHost to set
	 */
	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}
	/**
	 * @return the proxyPort
	 */
	public int getProxyPort() {
		return proxyPort;
	}
	/**
	 * @param proxyPort the proxyPort to set
	 */
	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}
	/**
	 * @return the authRequired
	 */
	public boolean isAuthRequired() {
		return authRequired;
	}
	/**
	 * @param authRequired the authRequired to set
	 */
	public void setAuthRequired(boolean authRequired) {
		this.authRequired = authRequired;
	}
	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}
	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

}
