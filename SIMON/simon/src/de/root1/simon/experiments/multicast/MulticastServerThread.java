package de.root1.simon.experiments.multicast;

/*
 * Copyright (c) 1995 - 2008 Sun Microsystems, Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.io.*;
import java.net.*;
import java.util.*;

public class MulticastServerThread extends Thread {

	private MulticastSocket socket;
	
	private InetAddress groupAddress = InetAddress.getByName("230.0.0.1");
	private int groupPort = 4446;

	public MulticastServerThread() throws IOException {
		super("MulticastServerThread");
		socket = new MulticastSocket(groupPort);
		socket.joinGroup(groupAddress);
	}

	public void run() {
		
		while (!interrupted()) {
			try {
				
				byte[] searchData = new byte[256];
				DatagramPacket searchPacket = new DatagramPacket(searchData,searchData.length);
				socket.receive(searchPacket);
				
				System.out.println(new String(searchPacket.getData())+" port="+searchPacket.getPort());
				
				// send answer pack to sender
				byte[] answerData = new Date().toString().getBytes();
				DatagramPacket answerPacket = new DatagramPacket(answerData, answerData.length, searchPacket.getAddress(), groupPort-1);
				socket.send(answerPacket);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		socket.close();
	}
	
	public static void main(String[] args) throws IOException {
        new MulticastServerThread().start();
	}
}
