package de.root1.simon;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

import de.root1.simon.Endpoint;

public class MethodWorker implements Runnable {
	private List queue = new LinkedList();
	
	public void processData(Endpoint server, SocketChannel socket, byte[] data, int count) {
		byte[] dataCopy = new byte[count];
		System.arraycopy(data, 0, dataCopy, 0, count);
		synchronized(queue) {
			queue.add(new EndpointDataEvent(server, socket, dataCopy));
			queue.notify();
		}
	}
	
	public void run() {
		EndpointDataEvent dataEvent;
		
		while(true) {
			// Wait for data to become available
			synchronized(queue) {
				while(queue.isEmpty()) {
					try {
						queue.wait();
					} catch (InterruptedException e) {
					}
				}
				dataEvent = (EndpointDataEvent) queue.remove(0);
			}
			
			// Return to sender
			dataEvent.endpoint.send(dataEvent.socket, dataEvent.data);
		}
	}
}
