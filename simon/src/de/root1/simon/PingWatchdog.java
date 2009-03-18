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
		private IoSession session;
		private int timeout;
		private Object monitor = new Object();
		private boolean pongReceived = false;

		public WaitRunnable(IoSession session) {
			this.session = session;
			this.timeout = dispatcher.getWriteTimeout()*1000;
		}

		public void run() {
			logger.debug("begin. sleeping {} ms before timout will occur. monitor={}", timeout, monitor);

			long start = System.currentTimeMillis();
			// only sleep if pong is not already received
			if (!pongReceived) {
				synchronized (monitor) {
					try {
						monitor.wait(timeout);
					} catch (InterruptedException e) {
					}
				}
			}
			long end = System.currentTimeMillis();

			boolean withinTime = end-start < timeout;
			long remaining = timeout-(end-start);
			long sleepTime = end-start;
			
			if (pongReceived) {
				
				if (withinTime) {
					logger.trace("Pong for session {} and monitor {} received within time! remaining={} rtt={}", new Object[]{Utils.longToHexString(session.getId()), monitor, remaining, sleepTime});
				} else {
					logger.warn("Pong for session {} and monitor {} received, but NOT WITHIN TIME! remaining={} rtt={}", new Object[]{Utils.longToHexString(session.getId()), monitor, remaining, sleepTime});
					closeSession();
				}
				
			} else {
				logger.trace("Pong for session {} and monitor {} not received", Utils.longToHexString(session.getId()), monitor);
				closeSession();
			}
	
			logger.debug("end.");
		}
		
		private void closeSession() {
			logger.debug("PingPong failure for session session {} and monitor {}. Closing it immediately.", Utils.longToHexString(session.getId()), monitor);
			session.close(true);
		}

		public void pingReceived(){
			logger.debug("Pong received for session {}. Notify monitor {}", Utils.longToHexString(session.getId()), monitor);
			pongReceived = true;
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
		WaitRunnable runnable = new WaitRunnable(session);
		sessionWaitrunnableMap.put(session, runnable);
		pingWatchdogPool.execute(runnable);
	}
	
	public void notifyPongReceived(IoSession session){
		logger.debug("Pong received for session: {}",Utils.longToHexString(session.getId()));
		WaitRunnable waitRunnable = sessionWaitrunnableMap.remove(session);
		if (waitRunnable!=null) {
			waitRunnable.pingReceived();
		} else {
			logger.debug("waitRunnable for {} isn't present anymore.", Utils.longToHexString(session.getId()));
		}
	}
		

}
