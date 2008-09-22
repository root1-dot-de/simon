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
package de.root1.simon.experiments.multicast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.logging.Logger;

import de.root1.simon.Statics;

public class PublishService extends Thread {

	
	protected transient Logger _log = Logger.getLogger(this.getClass().getName());
	
	
	private MulticastSocket socket;
	
	private InetAddress groupAddress = InetAddress.getByName("230.0.0.1");
	private int groupPort = 4446;

	private boolean shutdown;

	public PublishService() throws IOException {
		super("Simon.PublishService");
		socket = new MulticastSocket(groupPort);
		socket.joinGroup(groupAddress);
		socket.setSoTimeout(Statics.DEFAULT_SOCKET_TIMEOUT);
	}

	public void run() {
		
		while (!shutdown) {
			try {
				
				byte[] searchData = new byte[18];
				DatagramPacket searchPacket = new DatagramPacket(searchData,searchData.length);
				socket.receive(searchPacket);
				
				InetAddress requestAddress = searchPacket.getAddress();
				int requestPort = searchPacket.getPort();
				String requestString = new String(searchPacket.getData());
				
				System.out.println("requestHost="+requestAddress+" requestPort="+requestPort+" requestString="+requestString);
				
				if (requestString.equals(Statics.REQUEST_STRING)) {
					
					// send answer pack to sender
					byte[] answerData ="[SIMON|192.168.0.123:1234|myServer]".toString().getBytes();
					DatagramPacket answerPacket = new DatagramPacket(answerData, answerData.length, requestAddress, groupPort-1);
					socket.send(answerPacket);
					
					answerData ="[SIMON|192.168.123.123:1234|myServer2]".toString().getBytes();
					answerPacket = new DatagramPacket(answerData, answerData.length, requestAddress, groupPort-1);
					socket.send(answerPacket);

				}


			} catch (SocketTimeoutException e) {
				// do nothing on timeout
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		socket.close();
	}
	
	public void shutdown(){
		shutdown = true;
	}
	
	public static void main(String[] args) throws IOException {
        new PublishService().start();
	}
}
