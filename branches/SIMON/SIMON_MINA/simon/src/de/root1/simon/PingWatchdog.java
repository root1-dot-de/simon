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

import java.sql.Time;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.root1.simon.utils.Utils;

/**
 * A class that observes all ping-requests and closes session if ping echo isn't returned within a specified timeframe
 *
 * @author Alexander Christian
 * @version 200901141316
 *
 */
public class PingWatchdog {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	class WaitRunnable implements Runnable {

		private final Logger logger = LoggerFactory.getLogger(getClass());
		private Dispatcher dispatcher;
		private IoSession session;
		private int timeout;
		private Object monitor = new Object();
		private boolean echoReceived = true;

		public WaitRunnable(Dispatcher dispatcher, IoSession session) {
			this.dispatcher = dispatcher;
			this.session = session;
			this.timeout = dispatcher.getPingTimeOut()*1000;
		}

		public void run() {
			logger.debug("begin. sleeping {} ms before timout will occur.", timeout);

			long start = System.currentTimeMillis();
			synchronized (monitor) {
				try {
					monitor.wait(timeout);
				} catch (InterruptedException e) {
				}
			}
			long end = System.currentTimeMillis();

			logger.trace("sleep finished. duration: {} ms",(end-start));
			
			if (end-start < timeout) 
				logger.trace("Huuray. Pong received! remaining={} rtt={}", (timeout-(end-start)), (end-start));
				
			
			if (!echoReceived) {
				logger.debug("Ping timed out for session {}. Closing it immediately.", Utils.longToHexString(session.getId()));
				try {
					dispatcher.sessionClosed(session);
				} catch (Exception e) {
					logger.warn("Failed to close the session of ping-timeout client. Error: {}", e.getMessage());
				}
			} else {
				logger.debug("Ping-Pong for session {} ---> OKAY", Utils.longToHexString(session.getId()));
			}
			logger.debug("end.");
		}
		
		public void echoReceived(){
			logger.debug("Pong received for session: {}. Notify monitor.", Utils.longToHexString(session.getId()));
			echoReceived = true;
			synchronized (monitor) {
				monitor.notifyAll();
			}
		}
		
	}
	
	Map<IoSession, WaitRunnable> sessionWaitrunnableMap = Collections.synchronizedMap(new HashMap<IoSession, WaitRunnable>());
	ExecutorService pingWatchdogPool = Executors.newCachedThreadPool(new NamedThreadPoolFactory(Statics.PINGWATCHDOG_WORKERPOOL_NAME));
	private Dispatcher dispatcher;
	
	public PingWatchdog(Dispatcher dispatcher) {
		this.dispatcher=dispatcher;		
	}
	
	public void waitForPong(IoSession session){
		logger.debug("Waiting for pong for session: {}",Utils.longToHexString(session.getId()));
		WaitRunnable runnable = new WaitRunnable(dispatcher, session);
		sessionWaitrunnableMap.put(session, runnable);
		pingWatchdogPool.execute(runnable);
	}
	
	public void notifyPongReceived(IoSession session){
		logger.debug("Pong received for session: {}",Utils.longToHexString(session.getId()));
		WaitRunnable waitRunnable = sessionWaitrunnableMap.remove(session);
		waitRunnable.echoReceived();
	}
		

}
