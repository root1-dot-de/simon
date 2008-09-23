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


public class PublicationSearcher extends Thread {
	
	private static final int groupPort = 4446;
	private InetAddress groupAddress = InetAddress.getByName("230.0.0.1");
	private long searchTime = 2000;
	private List<SimonPublication> foundPublications;
	private boolean shutdown = false;
	private int searchProgress = 0;
	private List<SearchProgressListener> listeners = new ArrayList<SearchProgressListener>();
	

	public PublicationSearcher(SearchProgressListener listener, int searchTime) throws IOException {
		setName(Statics.PUBLISH_CLIENT_THREAD_NAME);
		foundPublications = new ArrayList<SimonPublication>();
		addSearchProgressListener(listener);
		this.searchTime = searchTime;
	}

	@Override
	public void run() {
		DatagramSocket socket;
		try {
			socket = new DatagramSocket(groupPort-1);
		
			byte[] requestData = Statics.REQUEST_STRING.getBytes();
			DatagramPacket searchPacket = new DatagramPacket(requestData,requestData.length, groupAddress, groupPort);
			socket.send(searchPacket);
			socket.setSoTimeout(Statics.DEFAULT_SOCKET_TIMEOUT); // set socket timeout to 100ms
	
			DatagramPacket packet;
	
			long startTime = System.currentTimeMillis();
			while (System.currentTimeMillis()<(startTime+searchTime) && !shutdown ) {
				
				try {
					byte[] buf = new byte[256];
					packet = new DatagramPacket(buf, buf.length);
					socket.receive(packet);
					String received = new String(packet.getData(), 0, packet.getLength());
					synchronized (foundPublications) {
						foundPublications.add(new SimonPublication(received));
					}
						
				} catch (SocketTimeoutException e) {
					// do nothing
				}
				
				searchProgress = (int)(100d/searchTime * (System.currentTimeMillis()-startTime));
				if (searchProgress>100) searchProgress = 100;
				updateListeners();
				
			}
			if (searchProgress!=100){
				searchProgress=100;
				updateListeners();
			}
			listeners.clear();
			socket.close();
			
		} catch (SocketException e1) {
			// TODO react on exception
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO react on exception
			e.printStackTrace();
		}
	}
	
	/**
	 * Shutdown and interrupt a search
	 */
	public void shutdown() {
		shutdown = true;
	}
	
	/**
	 * Returns new found publications
	 * @return  found publications since the last call of {@link PublicationSearcher#getNewPublications}
	 */
	public List<SimonPublication> getNewPublications(){
		List<SimonPublication> result;
		synchronized (foundPublications) {
			result = new ArrayList<SimonPublication>(foundPublications);
		}
		foundPublications.clear();
		return result;
	}
	
	/**
	 * Returns a value from 0..100 indicating the search progress. 0 is at beginning, 100 at end.
	 * @return value 0..100
	 */
	public int getSearchProgress(){
		return searchProgress;
	}
	
	/**
	 * Adds a new Listener to the list of listeners which have to be informed about the current status
	 * @param listener
	 */
	private void addSearchProgressListener(SearchProgressListener listener){
		if (listener!=null)
			listeners.add(listener);
	}
	
	/**
	 * Forwards the current search status to the registered listeners
	 * @param value
	 */
	private void updateListeners(){
		int numberOfObjects = 0;
		synchronized (foundPublications) {
			numberOfObjects = foundPublications.size();
		}
		for (SearchProgressListener listener : listeners) {
			listener.update(searchProgress, numberOfObjects);
		}
	}
	
	public boolean isSearching(){
		return isAlive();
	}
	
}
