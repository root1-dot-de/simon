package de.root1.simon;
import java.nio.channels.SocketChannel;

class EndpointDataEvent {
	public Endpoint endpoint;
	public SocketChannel socket;
	public byte[] data;
	
	public EndpointDataEvent(Endpoint endpoint, SocketChannel socket, byte[] data) {
		this.endpoint = endpoint;
		this.socket = socket;
		this.data = data;
	}
}