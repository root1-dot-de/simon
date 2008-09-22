package de.root1.simon;

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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;


public class PublishClient {
	
	private static final int groupPort = 4446;
	private InetAddress groupAddress = InetAddress.getByName("230.0.0.1");
	private long searchTime = 2000;
	

	public PublishClient() throws IOException {
	}
	
	public synchronized List<SimonPublishment> search(){
		List<SimonPublishment> result = new ArrayList<SimonPublishment>();
		DatagramSocket socket;
		try {
			socket = new DatagramSocket(groupPort-1);
		
			byte[] requestData = Statics.REQUEST_STRING.getBytes();
			DatagramPacket searchPacket = new DatagramPacket(requestData,requestData.length, groupAddress, groupPort);
			socket.send(searchPacket);
			socket.setSoTimeout(Statics.DEFAULT_SOCKET_TIMEOUT); // set socket timeout to 100ms
	
			DatagramPacket packet;
	
			long startTime = System.currentTimeMillis();
			while (System.currentTimeMillis()<(startTime+searchTime)) {
				
				try {
					byte[] buf = new byte[256];
					packet = new DatagramPacket(buf, buf.length);
					socket.receive(packet);
					String received = new String(packet.getData(), 0, packet.getLength());
					result.add(new SimonPublishment(received));
				} catch (SocketTimeoutException e) {
					// do nothing
				}
				
			}
			socket.close();
			
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	public static void main(String[] args) throws IOException {
		 PublishClient publishClient = new PublishClient();
		 List<SimonPublishment> search = publishClient.search();
		 for (SimonPublishment simonPublishment : search) {
			System.out.println(simonPublishment);
		}
	}

}