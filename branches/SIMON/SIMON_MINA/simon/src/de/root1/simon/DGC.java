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

import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * SIMON's "distributed garbage collector"
 * 
 * @author ACHR
 */
public class DGC extends Thread {
	
	// ---------------------
	private class PingWorker implements Runnable {

		protected transient Logger _log = Logger.getLogger(this.getClass().getName());
		private Dispatcher dispatcher;
		private SelectionKey key;

		public PingWorker(Dispatcher dispatcher, SelectionKey key) {
			this.dispatcher = dispatcher;
			this.key = key;
		}
		
		public void run() {
//			long rtt = dispatcher.sendPing(key);
//			if (_log.isLoggable(Level.FINER))
//				if (key.isValid())
//					_log.finer("rtt="+rtt+"ns, key="+Utils.getKeyIdentifierExtended(key));
//				else {
//					_log.finer("key removed from DGC. "+Utils.getKeyIdentifier(key));
//					dispatcher.getLookupTable().unreference(key);
//				}
//			
//			int port = ((SocketChannel)key.channel()).socket().getPort();
//			String address = ((SocketChannel)key.channel()).socket().getInetAddress().toString();
//			Simon.getStatistics().setRtt(address+":"+port, rtt);
			
		}
		
		
	}
	// ---------------------
	
	protected transient Logger _log = Logger.getLogger(this.getClass().getName());
	
	/** a reference to the dispatcher */
	private Dispatcher dispatcher = null;
	
	/** Shutdown-flag for this thread. Thread shuts down if flag is set to true. */
	private boolean shutdown;
	/** "is running" flag for this thread */
	private boolean isRunning = false;
	
	/** a pool which is used by the ping worker */
	private ExecutorService pingWorkerPool;
	
	/** a list of know <code>SelectionKey</code>s (means: clients) */
	private List<SelectionKey> clientKeyList = new ArrayList<SelectionKey>();

	/**
	 * 
	 * Constructor for the DGC.
	 * This only needs a reference to the dispatcher, where the {@link DGC.PingWorker} can execute the 
	 * {@link Dispatcher#sendPing(SelectionKey)} method.
	 * 
	 * @param dispatcher dispatcher-reference
	 */
	public DGC(Dispatcher dispatcher) {
		_log.fine("begin");
		this.dispatcher = dispatcher;
		this.pingWorkerPool = Executors.newSingleThreadExecutor(new NamedThreadPoolFactory(Statics.PINGWORKER_POOL_NAME));
		this.setName(Statics.SIMON_DGC_THREAD_NAME);
		_log.fine("end");
	}

	@Override
	public void run() {
		isRunning = true;
		while (!shutdown){
			
			// make a local copy
			List<SelectionKey> tempClientKeyList;
			synchronized (clientKeyList) {
				tempClientKeyList = new ArrayList<SelectionKey>(clientKeyList);				
			}
			_log.fine("Sending Pings to "+tempClientKeyList.size()+" clients");
			// for each known client in the local copy
			for (SelectionKey clientKey : tempClientKeyList) {
				
				if (_log.isLoggable(Level.FINEST))
					_log.finest("running ping-packet");
				
				// ... start a PingWorker.
				pingWorkerPool.execute(new PingWorker(dispatcher, clientKey));
				if (shutdown) break;
				
			}
			
			tempClientKeyList.clear();
			tempClientKeyList = null;

			if (shutdown) break;
			
			// after all pings are sent, wait Statics.DGC_INTERVAL milliseconds
			try {
				Thread.sleep(Statics.DGC_INTERVAL);
			} catch (InterruptedException e) {
				// nothing to do
			}
			
			
		}
		
		_log.fine("Shutdown pingWorkerPool.");
		pingWorkerPool.shutdownNow();
		while (!pingWorkerPool.isTerminated()){
			_log.finest("waiting for pingWorkerPool to shutdown...");
			try {
				Thread.sleep(Statics.WAIT_FOR_SHUTDOWN_SLEEPTIME);
			} catch (InterruptedException e){
				// do nothing
			}
		}
		_log.fine("pingWorkerPool is shutdown.");
		
		_log.fine("DGC is shutdown.");
		isRunning = false;
	}

	/**
	 * 
	 * Adds a {@link SelectionKey} to the list of keys observed by the DGC
	 * 
	 * @param connectedClientKey the key to add
	 */
	public void addKey(SelectionKey connectedClientKey) {
		_log.finest("Adding client key to dgc list");
		synchronized (clientKeyList) {
			clientKeyList.add(connectedClientKey);				
		}
	}

	/**
	 * Shutdown the DGC. This blocks until shutdown of DGC is finished.
	 *
	 */
	public void shutdown() {
		_log.fine("begin");
		shutdown = true;
		interrupt();
		while (isRunning) {
			_log.finest("waiting for DGC to shutdown...");
			try {
				Thread.sleep(Statics.WAIT_FOR_SHUTDOWN_SLEEPTIME);
			} catch (InterruptedException e) {
				// nothing to do
			}
		}
		_log.fine("end");
	}

	/**
	 * 
	 * Returns whether the DGC is running or not
	 * 
	 * @return true, if DGC is running, false if not
	 */
	public boolean isRunning() {
		return isRunning;
	}

	/**
	 * 
	 * Removes a key that is observed by the DGC.
	 * 
	 * @param key the key to remove
	 */
	public void removeKey(SelectionKey key) {
		synchronized (clientKeyList) {
			clientKeyList.remove(key);
		}
	}
}
