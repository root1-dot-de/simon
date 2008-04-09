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
package de.root1.simon;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import de.root1.simon.utils.Utils;


/**
 * TODO Documentation to be done
 *
 * @author achristian
 *
 */
public class Registry extends Thread {
	
	private LookupTable serverLookupTable = null;
	private int port;
	
	// -----------------
	// NIO Stuff
	

	
	// The selector we'll be monitoring
	private Dispatcher dispatcher;
	private Acceptor acceptor;
	private ExecutorService threadPool;

	/**
	 * TODO Documentation to be done
	 * @param lookupTable
	 * @param port
	 */
	public Registry(LookupTable lookupTable, int port, ExecutorService threadPool) {
		this.serverLookupTable = lookupTable;
		this.port = port;
		this.threadPool = threadPool;
		this.setName("SimonRegistry");
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		//Utils.debug("Registry.run() -> start");
		
		try {
			
			dispatcher = new Dispatcher(serverLookupTable, threadPool);
			new Thread(dispatcher,"Simon.Registry.Dispatcher").start();

			acceptor = new Acceptor(dispatcher,port);
			new Thread(acceptor,"Simon.Registry.Acceptor").start();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Utils.debug("Registry.run() -> end");
	}
	
	

	
	

	
}
