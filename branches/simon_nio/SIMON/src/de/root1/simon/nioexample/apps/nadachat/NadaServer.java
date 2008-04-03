package de.root1.simon.nioexample.apps.nadachat;

import de.root1.simon.nioexample.impl.DumbBufferFactory;
import de.root1.simon.nioexample.impl.IInputHandlerFactory;
import de.root1.simon.nioexample.impl.Dispatcher;
import de.root1.simon.nioexample.impl.Acceptor;
import de.root1.simon.nioexample.interfaces.IByteBufferFactory;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class NadaServer {

	private NadaServer() {
		// cannot instantiate
	}

	public static void main(String[] args) throws IOException {

		Executor executor = Executors.newCachedThreadPool();

		IByteBufferFactory bufFactory = new DumbBufferFactory(512);

		Dispatcher dispatcher = new Dispatcher(executor, bufFactory);

		IInputHandlerFactory factory = new NadaProtocol();

		Acceptor acceptor = new Acceptor(1234, dispatcher, factory);

		dispatcher.start();
		acceptor.newThread();
	}
}
