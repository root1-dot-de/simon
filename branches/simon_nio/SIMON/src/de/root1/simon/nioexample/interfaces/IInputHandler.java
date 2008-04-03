package de.root1.simon.nioexample.interfaces;

import java.nio.ByteBuffer;

public interface IInputHandler {
	ByteBuffer nextMessage(IChannelFacade channelFacade);
	void handleInput(ByteBuffer message, IChannelFacade channelFacade);

	void starting(IChannelFacade channelFacade);
	void started(IChannelFacade channelFacade);
	void stopping(IChannelFacade channelFacade);
	void stopped(IChannelFacade channelFacade);
}
