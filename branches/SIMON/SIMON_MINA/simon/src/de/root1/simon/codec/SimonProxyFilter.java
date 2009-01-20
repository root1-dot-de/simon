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

import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.filterchain.IoFilterChain.Entry;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO document me
 * @author Alexander Christian
 * @version 200901291551
 *
 */
public class SimonProxyFilter extends IoFilterAdapter {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private boolean connected = false;
	private boolean okReceived;
	private List<Entry> filterListBackup;
	
	private String targetHost;
	private int targetPort;
	private boolean authRequired;
	private String username;
	private String password;
	private String receivedAnswerMsg = "";
	
	public SimonProxyFilter(String targetHost, int targetPort, boolean authRequired, String username, String password) {
		this.targetHost = targetHost;
		this.targetPort = targetPort;
		this.authRequired = authRequired;
		this.username = username;
		this.password = password;
		
		logger.debug("Proxyfilter loaded");
	}
	
	@Override
	public void sessionCreated(NextFilter nextFilter, IoSession session)
			throws Exception {
		logger.debug("session created: {}",session);
		if (!connected){
			logger.debug("making proxy tunnel connection");
			
			logger.debug("backup filterchain: {}", session.getFilterChain());
			filterListBackup = session.getFilterChain().getAll();
			
			session.getFilterChain().clear();
			session.getFilterChain().addLast("textlinecodec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));
			session.getFilterChain().addLast("proxy", this);
			
			logger.debug("new temporary filterchain: {}", session.getFilterChain());
			
			logger.debug("sending proxy connect request");
			session.write("CONNECT "+targetHost+":"+targetPort+" HTTP/1.1");
			session.write("Host: "+targetHost+":"+targetPort+"");
			if (authRequired) 
				session.write("Proxy-Authorization: Basic "+new String(Base64.encodeBase64((username+":"+password).getBytes())));
			session.write("");
		}
	}
	
	@Override
	public void messageReceived(NextFilter nextFilter, IoSession session,
			Object message) throws Exception {
		logger.debug("message="+message);
		
		receivedAnswerMsg += message+"\n";
		if (message.toString().contains("HTTP/1.1 200")) {
			logger.debug("OK received");
			okReceived = true;
		}
		
		if (message.toString().equals("")){
			
			if (okReceived) {
				logger.debug("rest of OK header received. restoring filterchain");
				session.getFilterChain().clear();
				for (Entry entry : filterListBackup) {
					session.getFilterChain().addLast(entry.getName(), entry.getFilter());
				}
				session.getFilterChain().remove("simonproxyfilter");
				logger.debug("filterchain restored: {}",session.getFilterChain());
				session.getFilterChain().fireSessionCreated();
			} else {
				throw new Exception("Creating tunnel failed. Answer from proxyserver was: \n"+receivedAnswerMsg);
			}
		}
	}
	
}
