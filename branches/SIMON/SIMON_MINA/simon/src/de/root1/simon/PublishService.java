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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class PublishService extends Thread {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private MulticastSocket socket;
	private InetAddress groupAddress = InetAddress.getByName("230.0.0.1");
	private int groupPort = 4446;
	private boolean shutdown;
	private List<SimonPublication> publishments;

	protected PublishService(List<SimonPublication> publishments) throws IOException {
		logger.debug("preparing publish service");
		setName(Statics.PUBLISH_SERVICE_THREAD_NAME);
		socket = new MulticastSocket(groupPort);
		socket.joinGroup(groupAddress);
		socket.setSoTimeout(Statics.DEFAULT_SOCKET_TIMEOUT);
		this.publishments = publishments;
	}

	public void run() {
		logger.debug("publish service up and running");
		while (!shutdown) {
			try {
				
				byte[] searchData = new byte[Statics.REQUEST_STRING.length()];
				DatagramPacket searchPacket = new DatagramPacket(searchData,searchData.length);
				socket.receive(searchPacket);
				
				InetAddress requestAddress = searchPacket.getAddress();
				int requestPort = searchPacket.getPort();
				String requestString = new String(searchPacket.getData());
				
				logger.debug("got 'find server' request. requestHost="+requestAddress+" requestPort="+requestPort+" requestString="+requestString);
				
				if (requestString.equals(Statics.REQUEST_STRING)) {
					
					// send answer pack to sender
					for (SimonPublication publishment : publishments) {
						logger.debug("answering: "+publishment);
						byte[] answerData = publishment.toString().getBytes();
						DatagramPacket answerPacket = new DatagramPacket(answerData, answerData.length, requestAddress, groupPort-1);
						socket.send(answerPacket);
					}
					
				}


			} catch (SocketTimeoutException e) {
				// do nothing on timeout
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		socket.close();
		logger.debug("publish service terminated!");
	}
	
	protected void shutdown(){
		shutdown = true;
		logger.debug("Shutting down the publish service now ...");
	}
	
}
