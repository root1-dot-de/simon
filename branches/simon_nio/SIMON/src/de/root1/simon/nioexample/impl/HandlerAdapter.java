package de.root1.simon.nioexample.impl;

import de.root1.simon.nioexample.interfaces.IByteBufferFactory;
import de.root1.simon.nioexample.interfaces.IChannelFacade;
import de.root1.simon.nioexample.interfaces.IInputHandler;
import de.root1.simon.nioexample.interfaces.IInputQueue;
import de.root1.simon.nioexample.interfaces.IOutputQueue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Callable;
import java.net.SocketException;

/**
 * 
 * TODO Documentation to be done
 *
 * @author achristian
 *
 */
class HandlerAdapter implements Callable<HandlerAdapter>, IChannelFacade {

	private final Dispatcher dispatcher;
	private final IInputQueue inputQueue;
	private final IOutputQueue outputQueue;
	private final Object stateChangeLock = new Object();
	private IInputHandler clientHandler;
	private SelectionKey key = null;
	private SelectableChannel channel = null;
	private volatile int interestOps = 0;
	private int readyOps = 0;
	private boolean shuttingDown = false;
	private volatile boolean running = false;
	private volatile boolean dead = false;

	/**
	 * 
	 * TODO Documentation to be done
	 * @param clientHandler
	 * @param dispatcher
	 * @param bufferFactory
	 */
	public HandlerAdapter(IInputHandler clientHandler, Dispatcher dispatcher, IByteBufferFactory bufferFactory) {
		this.dispatcher = dispatcher;
		this.clientHandler = clientHandler;

		inputQueue = new InputQueue(bufferFactory);
		outputQueue = new OutputQueue(bufferFactory, this);
	}

	// ------------------------------------------------------------
	// Implementation of Callable<HandlerAdapter> interface

	public HandlerAdapter call() throws IOException {
		try {
			drainOutput();
			fillInput();

			ByteBuffer message;

			// must process all buffered messages because Selector will
			// not fire again for input that's already read and buffered
			while ((message = clientHandler.nextMessage(this)) != null) {
				clientHandler.handleInput(message, this);
			}
			
		} finally {
			synchronized (stateChangeLock) {
				running = false;
			}
		}

		return this;
	}

	// ------------------------------------------------------------
	// Implementation of ChannelFacade interface
	public IInputQueue inputQueue() {
		return inputQueue;
	}

	public IOutputQueue outputQueue() {
		return outputQueue;
	}

	public void modifyInterestOps(int opsToSet, int opsToReset) {
		synchronized (stateChangeLock) {
			interestOps = (interestOps | opsToSet) & (~opsToReset);

			if (!running) {
				dispatcher.enqueueStatusChange(this);
			}
		}
	}

	public int getInterestOps() {
		return (interestOps);
	}

	public void setHandler(IInputHandler handler) {
		clientHandler = handler;
	}

	// --------------------------------------------------
	// package-local methods called by NioDispatcher

	void prepareToRun() {
		synchronized (stateChangeLock) {
			interestOps = key.interestOps();
			readyOps = key.readyOps();
			running = true;
		}
	}

	void setKey(SelectionKey key) {
		this.key = key;
		channel = key.channel();
		interestOps = key.interestOps();
	}

	SelectionKey key() {
		return key;
	}

	boolean isDead() {
		return dead;
	}

	// Cause the channel to be de-registered from the Selector upon return
	void die() {
		dead = true;
	}

	// --------------------------------------------------
	// package-local life-cycle helper methods, called by NioDispatcher

	// Called when registering, but before the handler is active
	void registering() {
		clientHandler.starting(this);
	}

	// Called when the handler is registered, but before the first message
	void registered() {
		clientHandler.started(this);
	}

	// Called when unregistration has been requested, but while the
	// handler is still active and able to interact with the framework.
	// Extension Point: This implementation simply calls through to
	// the client handler, which may or may not be running. Either
	// the client code must take steps to protect its internal state,
	// or logic could be added here to wait until the handler finishes.
	void unregistering() {
		clientHandler.stopping(this);
	}

	// Called when the handler has been unregistered and is no longer active.
	// If unregistering() waits for the handler to finish, then this
	// one should be safe. If not, then this function has the same
	// concurrency concerns as does unregistering().
	void unregistered() {
		clientHandler.stopped(this);
	}

	// --------------------------------------------------
	// Private helper methods

	// These three methods manipulate the private copy of the selection
	// interest flags. Upon completion, this local copy will be copied
	// back to the SelectionKey as the new interest set.
	private void enableWriteSelection() {
		modifyInterestOps(SelectionKey.OP_WRITE, 0);
	}

	private void disableWriteSelection() {
		modifyInterestOps(0, SelectionKey.OP_WRITE);
	}

	private void disableReadSelection() {
		modifyInterestOps(0, SelectionKey.OP_READ);
	}

	// If there is output queued, and the channel is ready to
	// accept data, send as much as it will take.
	private void drainOutput() throws IOException {
		if (((readyOps & SelectionKey.OP_WRITE) != 0)
				&& (!outputQueue.isEmpty())) {
			outputQueue.drainTo((ByteChannel) channel);
		}

		// Write selection is turned on when output data in enqueued,
		// turn it off when the queue becomes empty.
		if (outputQueue.isEmpty()) {
			disableWriteSelection();

			if (shuttingDown) {
				channel.close();
				clientHandler.stopped(this);
			}
		}
	}

	// Attempt to fill the input queue with as much data as the channel
	// can provide right now. If end-of-stream is reached, stop read
	// selection and shutdown the input side of the channel.
	private void fillInput() throws IOException {
		if (shuttingDown)
			return;

		int rc = inputQueue.fillFrom((ByteChannel) channel);

		if (rc == -1) {
			disableReadSelection();

			if (channel instanceof SocketChannel) {
				SocketChannel sc = (SocketChannel) channel;

				if (sc.socket().isConnected()) {
					try {
						sc.socket().shutdownInput();
					} catch (SocketException e) {
						// happens sometimes, ignore
					}
				}
			}

			shuttingDown = true;
			clientHandler.stopping(this);

			// cause drainOutput to run, which will close
			// the socket if/when the output queue is empty
			enableWriteSelection();
		}
	}
}
