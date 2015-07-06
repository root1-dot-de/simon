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
package de.root1.simon.codec;

import java.nio.charset.Charset;
import java.util.List;

//import org.apache.mina.core.filterchain.IoFilterAdapter;
//import org.apache.mina.core.filterchain.IoFilterChain;
//import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
//import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
//import org.apache.mina.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.root1.simon.SimonProxyConfig;
import de.root1.simon.utils.FilterEntry;

/**
 * TODO document me
 * @author Alexander Christian
 * @version 200901291551
 *
 */
public class SimonProxyFilter extends  {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public static final String FILTER_NAME = SimonProxyFilter.class.getName();
	
	/** a flag that indicates whether the proxy has yet returned a "http okay" or not */
	private boolean okReceived;
	
	/** the host of the simon server behind the proxy */
	private String targetHost;
	
	/** The port of the simon server behind the proxy */
	private int targetPort;
	
	/** Does the proxy require auth? */
	private boolean authRequired;
	
	/** the username used to auth with the proxy */
	private String username;
	
	/** the password used to auth with the proxy */
	private String password;
	
	/** for summing up the answer-lines from the proxy */
	private String receivedAnswerMsg = "";

	/**
	 * the filters to add after proxy connection is established
	 */
	private List<FilterEntry> filters;

	/**
	 * TODO document me
	 * @param targetHost
	 * @param targetPort
	 * @param proxyConfig
	 * @param backupChain 
	 */
	public SimonProxyFilter(String targetHost, int targetPort, SimonProxyConfig proxyConfig, List<FilterEntry> backupChain) {
		this.targetHost = targetHost;
		this.targetPort = targetPort;
		this.authRequired = proxyConfig.isAuthRequired();
		this.username = proxyConfig.getUsername();
		this.password = proxyConfig.getPassword();
		this.filters = backupChain;
		logger.debug("Proxyfilter loaded");
	}
	
	@Override
	public void sessionCreated(NextFilter nextFilter, IoSession session)
			throws Exception {

		logger.debug("session created: {}",session);
		IoFilterChain filterChain = session.getFilterChain();
		
		filterChain.clear();
		
		// add filters
		if (logger.isTraceEnabled())
			filterChain.addLast( LoggingFilter.class.getName(), new LoggingFilter() );
		filterChain.addLast(TextLineCodecFactory.class.getName(), new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));
		filterChain.addLast(this.getClass().getName(), this);
		
		logger.trace("ready for proxy connection. chain is now: {}",filterChain);
		
		logger.debug("sending proxy connect request");
		session.write("CONNECT "+targetHost+":"+targetPort+" HTTP/1.1");
		
		/*
		 * Calling "session.write("");" for sending a "new line" results in "ssl problems". 
		 * It seems that the empty string is "too small" to send at once. 
		 * So it get's delayed and is sent if the next "big" messages are sent. 
		 * But at this time, the filters are replaced and SSL is already used. 
		 * So one have to make sure that all data is sent before replacing the protocol.
		 * To prevent this, we simply send the additional "line feed" as an "\n" with the last 
		 * command line
		 */

		if (authRequired) {
			session.write("Host: "+targetHost+":"+targetPort+"");
			session.write("Proxy-Authorization: Basic "+new String(Base64.encodeBase64((username+":"+password).getBytes()))+"\n");
		} else {
			session.write("Host: "+targetHost+":"+targetPort+"\n");
		}
		
		
	}
	
	@Override
	public void messageReceived(NextFilter nextFilter, IoSession session,
			Object message) throws Exception {
		logger.debug("message="+message);
		
		// sum up the answer-lines
		receivedAnswerMsg += message+"\n";
		
		// check for the "http okay" message 
		if (message.toString().contains("HTTP/1.1 200")) {
			logger.debug("OK detected");
			okReceived = true;
		}
		
		// if an empty line is read ...
		if (message.toString().equals("")){
			
			// and the "http okay" was received, the connection is successfully established
			if (okReceived) {
				logger.debug("rest of OK header received. restore 'normal' filterchain");
				session.getFilterChain().clear();
				
				for (FilterEntry relation : filters) {
					session.getFilterChain().addLast(relation.name, relation.filter);
				}
				logger.trace("restored. chain is now: {}",session.getFilterChain());
				session.getFilterChain().fireSessionCreated();
				
			} else {
				// otherwise the connection could not be established. 
				// So throw the exception with the corresponding message from the proxy
				throw new Exception("Creating tunnel failed. Answer from proxyserver was: \n"+receivedAnswerMsg);
			}
		}
		
	}
	
}
