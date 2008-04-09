package de.root1.simon.nioexample.apps.echo;

import java.nio.ByteBuffer;

import de.root1.simon.nioexample.interfaces.IChannelFacade;
import de.root1.simon.nioexample.interfaces.IInputHandler;
import de.root1.simon.nioexample.interfaces.IInputQueue;

public class EchoHandler implements IInputHandler {

	public ByteBuffer nextMessage(IChannelFacade channelFacade) {
		IInputQueue inputQueue = channelFacade.inputQueue();
		int nlPos = inputQueue.indexOf((byte) '\n');

		if (nlPos == -1)
			return (null);

		return (inputQueue.dequeueBytes(nlPos + 1));
	}

	public void handleInput(ByteBuffer message, IChannelFacade channelFacade) {
		channelFacade.outputQueue().enqueue(message);
	}

	public void starting(IChannelFacade channelFacade) {
		// nothing
	}

	public void started(IChannelFacade channelFacade) {
		// nothing
	}

	public void endOfInput(IChannelFacade channelFacade) {
		// nothing
	}

	public void stopping(IChannelFacade channelFacade) {
		// nothing
	}

	public void stopped(IChannelFacade channelFacade) {
		// nothing
	}
}
