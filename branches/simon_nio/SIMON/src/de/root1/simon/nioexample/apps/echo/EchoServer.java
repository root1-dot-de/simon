package de.root1.simon.nioexample.apps.echo;

import de.root1.simon.nioexample.impl.DumbBufferFactory;
import de.root1.simon.nioexample.impl.GenericInputHandlerFactory;
import de.root1.simon.nioexample.impl.Dispatcher;
import de.root1.simon.nioexample.impl.Acceptor;
import de.root1.simon.nioexample.interfaces.IByteBufferFactory;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class EchoServer {

	private EchoServer() {
		// cannot instantiate
	}

	public static void main(String[] args) throws IOException {
		Executor dispatcherPool = Executors.newCachedThreadPool();

		IByteBufferFactory bufFactory = new DumbBufferFactory(1024);

		Dispatcher dispatcher = new Dispatcher(dispatcherPool, bufFactory);

		Acceptor acceptor = new Acceptor(1234, dispatcher,
				new GenericInputHandlerFactory(EchoHandler.class));

		dispatcher.start();
		acceptor.newThread();
		acceptor.newThread();
		acceptor.newThread();
	}
}
