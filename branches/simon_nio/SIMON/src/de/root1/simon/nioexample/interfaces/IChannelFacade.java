package de.root1.simon.nioexample.interfaces;

public interface IChannelFacade {
	IInputQueue inputQueue();
	IOutputQueue outputQueue();
	void setHandler(IInputHandler handler);
	int getInterestOps();
	void modifyInterestOps(int opsToSet, int opsToReset);
}
