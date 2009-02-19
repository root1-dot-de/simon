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
 * A pojo class that holds information about the proxy configuration 
 * needed to establish a SIMON connection via a proxy server
 * 
 * @author Alexander Christian
 * @version 200901201553
 *
 */
public class SimonProxyConfig {

	/**
	 * Constructor for the proxy config class. Let's one set all needed
	 * information at once. if authRequired==null, username and password can be null
	 * 
	 * @param proxyHost the proxy's host
	 * @param proxyPort the proxy's port
	 * @param authRequired does the proxy require an authentication?
	 * @param username username, if authentication is required
	 * @param password password, if authentication is required
	 */
	public SimonProxyConfig(String proxyHost, int proxyPort,
			boolean authRequired, String username, String password) {
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;
		this.authRequired = authRequired;
		this.username = username;
		this.password = password;
	}
	
	/**
	 * Default "empty" constructor. Please use the setter to define the proxy.
	 */
	public SimonProxyConfig() {
	}

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
	 * Returns the current set proxy host
	 * @return the proxy's host
	 */
	public String getProxyHost() {
		return proxyHost;
	}
	
	/**
	 * Sets the proxy host to connect to
	 * @param proxyHost the proxyHost to connect to
	 */
	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}
	
	/**
	 * Returns the current set port on which the proxy listens for incoming connections
	 * @return the proxyPort
	 */
	public int getProxyPort() {
		return proxyPort;
	}
	
	/**
	 * Sets the port on which the proxy listens for incoming connections
	 * @param proxyPort the proxy's port
	 */
	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}
	
	/**
	 * Returns the current set "authentication required?" flag
	 * @return true, if flag is set, false if not
	 */
	public boolean isAuthRequired() {
		return authRequired;
	}
	
	/**
	 * Sets whether the proxy needs authentication or not
	 * @param authRequired true, if auth. is required, false if not
	 */
	public void setAuthRequired(boolean authRequired) {
		this.authRequired = authRequired;
	}
	
	/**
	 * Gets the username that was set for proxy authentication
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}
	
	/**
	 * Sets the username that is needed for proxy authentication
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	
	/**
	 * Gets the password that was set for proxy authentication
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
	
	/**
	 * Sets the password that is needed for proxy authentication
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

}
