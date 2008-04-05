package de.root1.simon.nioexample.apps.chat;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import de.root1.simon.nioexample.impl.Acceptor;
import de.root1.simon.nioexample.impl.Dispatcher;
import de.root1.simon.nioexample.impl.DumbBufferFactory;
import de.root1.simon.nioexample.interfaces.IByteBufferFactory;
import de.root1.simon.nioexample.interfaces.IInputHandlerFactory;

public class ChatServer
{
	private ChatServer()
	{
		// cannot instantiate
	}

	public static void main (String [] args)
		throws IOException
	{
		Executor executor = Executors.newCachedThreadPool();
		IByteBufferFactory bufFactory = new DumbBufferFactory (1024);
		Dispatcher dispatcher = new Dispatcher (executor, bufFactory);
		IInputHandlerFactory factory = new ChatProtocol();
		Acceptor acceptor = new Acceptor (1234, dispatcher, factory);

		dispatcher.start();
		acceptor.newThread();
	}
}
