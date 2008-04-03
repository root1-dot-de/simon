package de.root1.simon.nioexample.impl;

import de.root1.simon.nioexample.interfaces.IDispatcher;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Acceptor {
	private final IDispatcher dispatcher;
	private final IInputHandlerFactory inputHandlerFactory;
	private final ServerSocketChannel listenSocket;
	private final Listener listener;
	private final List<Thread> threads = new ArrayList<Thread>();
	private Logger logger = Logger.getLogger(getClass().getName());
	private volatile boolean running = true;

	public Acceptor(ServerSocketChannel listenSocket, IDispatcher dispatcher,
			IInputHandlerFactory inputHandlerFactory) {
		this.listenSocket = listenSocket;
		this.dispatcher = dispatcher;
		this.inputHandlerFactory = inputHandlerFactory;

		listener = new Listener();
	}

	public Acceptor(SocketAddress socketAddress, IDispatcher dispatcher,
			IInputHandlerFactory inputHandlerFactory) throws IOException {
		this(ServerSocketChannel.open(), dispatcher, inputHandlerFactory);

		listenSocket.socket().bind(socketAddress);
	}

	public Acceptor(int port, IDispatcher dispatcher,
			IInputHandlerFactory inputHandlerFactory) throws IOException {
		this(new InetSocketAddress(port), dispatcher, inputHandlerFactory);
	}

	private class Listener implements Runnable {
		public void run() {
			while (running) {
				try {
					SocketChannel client = listenSocket.accept();

					if (client == null) {
						continue;
					}

					dispatcher.registerChannel(client, inputHandlerFactory
							.newHandler());

				} catch (ClosedByInterruptException e) {
					logger
							.fine("ServerSocketChannel closed by interrupt: "
									+ e);
					return;

				} catch (ClosedChannelException e) {
					logger.log(Level.SEVERE,
							"Exiting, serverSocketChannel is closed: " + e, e);
					return;

				} catch (Throwable t) {
					logger.log(Level.SEVERE,
							"Exiting, Unexpected Throwable doing accept: " + t,
							t);

					try {
						listenSocket.close();
					} catch (Throwable e1) { /* nothing */
					}

					return;
				}
			}
		}
	}

	public synchronized Thread newThread() {
		Thread thread = new Thread(listener);

		threads.add(thread);

		thread.start();

		return thread;
	}

	public synchronized void shutdown() {
		running = false;

		for (Iterator it = threads.iterator(); it.hasNext();) {
			Thread thread = (Thread) it.next();

			if ((thread != null) && (thread.isAlive())) {
				thread.interrupt();
			}
		}

		for (Iterator it = threads.iterator(); it.hasNext();) {
			Thread thread = (Thread) it.next();

			try {
				thread.join();
			} catch (InterruptedException e) {
				// nothing
			}

			it.remove();
		}

		try {
			listenSocket.close();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Caught an exception shutting down", e);
		}
	}
}
