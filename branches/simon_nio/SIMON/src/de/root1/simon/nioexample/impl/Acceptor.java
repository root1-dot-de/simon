package de.root1.simon.nioexample.impl;

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

import de.root1.simon.nioexample.interfaces.IDispatcher;

/**
 * 
 * TODO Documentation to be done
 *
 *
 */
public class Acceptor {
	
	/** the dispatcher */
	private final IDispatcher dispatcher;
	
	/** for creating an inputhandler */
	private final IInputHandlerFactory inputHandlerFactory;
	
	/** the socketchannel the server is listening on */
	private final ServerSocketChannel listenSocket;
	
	/** the internal listener object/class */
	private final Listener listener;
	
	/** a list with threads... for what???? */
	private final List<Thread> threads = new ArrayList<Thread>();
	
	private Logger logger = Logger.getLogger(getClass().getName());
	private volatile boolean running = true;

	/**
	 * 
	 * Creates a new Acceptor runnable
	 * @param listenSocket the ServerSocketChannel to listen for new connections on
	 * @param dispatcher the used dispatcher 
	 * @param inputHandlerFactory the used input handler factory
	 */
	public Acceptor(ServerSocketChannel listenSocket, IDispatcher dispatcher, IInputHandlerFactory inputHandlerFactory) {
		this.listenSocket = listenSocket;
		this.dispatcher = dispatcher;
		this.inputHandlerFactory = inputHandlerFactory;

		listener = new Listener();
	}

	/**
	 * 
	 * TODO Documentation to be done
	 * @param socketAddress
	 * @param dispatcher
	 * @param inputHandlerFactory
	 * @throws IOException
	 */
	public Acceptor(SocketAddress socketAddress, IDispatcher dispatcher,
			IInputHandlerFactory inputHandlerFactory) throws IOException {
		this(ServerSocketChannel.open(), dispatcher, inputHandlerFactory);

		listenSocket.socket().bind(socketAddress);
	}

	/**
	 * 
	 * TODO Documentation to be done
	 * @param port
	 * @param dispatcher
	 * @param inputHandlerFactory
	 * @throws IOException
	 */
	public Acceptor(int port, IDispatcher dispatcher,
			IInputHandlerFactory inputHandlerFactory) throws IOException {
		this(new InetSocketAddress(port), dispatcher, inputHandlerFactory);
	}

	/**
	 * 
	 * TODO Documentation to be done
	 *
	 * @author achristian
	 *
	 */
	private class Listener implements Runnable {
		public void run() {
			while (running) {
				try {
					System.out.println("Listener waiting for accept ...! "+this);
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

	/**
	 * starts a new acceptor-thread
	 * @return the started aceptor thread
	 */
	public synchronized Thread newThread() {
		
		Thread thread = new Thread(listener);
		threads.add(thread);
		thread.start();

		return thread;
	}

	/**
	 * Shuts down each started acceptor 
	 */
	public synchronized void shutdown() {
		running = false;

		// signal each thread the shutdown
		for (Iterator it = threads.iterator(); it.hasNext();) {
			Thread thread = (Thread) it.next();

			if ((thread != null) && (thread.isAlive())) {
				thread.interrupt();
			}
		}

		// wait for each thread to die
		for (Iterator it = threads.iterator(); it.hasNext();) {
			Thread thread = (Thread) it.next();

			try {
				thread.join();
			} catch (InterruptedException e) {
				// nothing
			}
			// remove the thread
			it.remove();
		}

		try {
			listenSocket.close();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Caught an exception shutting down", e);
		}
	}
}
