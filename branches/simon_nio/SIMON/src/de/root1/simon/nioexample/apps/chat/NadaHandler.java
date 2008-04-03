package de.root1.simon.nioexample.apps.chat;

import de.root1.simon.nioexample.interfaces.IChannelFacade;
import de.root1.simon.nioexample.interfaces.IInputHandler;
import de.root1.simon.nioexample.interfaces.IInputQueue;

import java.nio.ByteBuffer;

public class NadaHandler implements IInputHandler {
	private final NadaProtocol protocol;

	public NadaHandler(NadaProtocol protocol) {
		this.protocol = protocol;
	}

	// --------------------------------------------------------
	// Implementation of the InputHandler interface

	public ByteBuffer nextMessage(IChannelFacade channelFacade) {
		IInputQueue inputQueue = channelFacade.inputQueue();
		int nlPos = inputQueue.indexOf((byte) '\n');

		if (nlPos == -1)
			return null;

		if ((nlPos == 1) && (inputQueue.indexOf((byte) '\r') == 0)) {
			inputQueue.discardBytes(2); // eat CR/NL by itself
			return null;
		}

		return (inputQueue.dequeueBytes(nlPos + 1));
	}

	public void handleInput(ByteBuffer message, IChannelFacade channelFacade) {
		protocol.handleMessage(channelFacade, message);
	}

	public void starting(IChannelFacade channelFacade) {
		// System.out.println ("NadaHandler: starting");
	}

	public void started(IChannelFacade channelFacade) {
		protocol.newUser(channelFacade);
	}

	public void stopping(IChannelFacade channelFacade) {
		// System.out.println ("NadaHandler: stopping");
	}

	public void stopped(IChannelFacade channelFacade) {
		protocol.endUser(channelFacade);
	}
}