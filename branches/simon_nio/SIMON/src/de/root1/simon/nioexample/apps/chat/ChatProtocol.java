package de.root1.simon.nioexample.apps.chat;

import de.root1.simon.nioexample.interfaces.IChannelFacade;
import de.root1.simon.nioexample.interfaces.IInputHandler;
import de.root1.simon.nioexample.interfaces.IInputHandlerFactory;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ChatProtocol implements IInputHandlerFactory {

	Map<IChannelFacade, NadaUser> users = Collections
			.synchronizedMap(new HashMap<IChannelFacade, NadaUser>());

	// --------------------------------------------------
	// Implementation of InputHandlerFactory interface
	public IInputHandler newHandler() throws IllegalAccessException,
			InstantiationException {
		return new ChatHandler(this);
	}
	// --------------------------------------------------

	void newUser(IChannelFacade facade) {
		NadaUser user = new NadaUser(facade);

		users.put(facade, user);
		user.send(ByteBuffer.wrap((user.getNickName() + "\n").getBytes()));
	}

	void endUser(IChannelFacade facade) {
		users.remove(facade);
	}

	public void handleMessage(IChannelFacade facade, ByteBuffer message) {
		broadcast(users.get(facade), message);
	}

	private void broadcast(NadaUser sender, ByteBuffer message) {
		synchronized (users) {
			for (NadaUser user : users.values()) {
				if (user != sender) {
					sender.sendTo(user, message);
				}
			}
		}
	}

	// ----------------------------------------------------
	private static class NadaUser {
		private final IChannelFacade facade;
		private String nickName;
		private ByteBuffer prefix = null;
		private static int counter = 1;

		public NadaUser(IChannelFacade facade) {
			this.facade = facade;
			setNickName("nick-" + counter++);
		}

		public void send(ByteBuffer message) {
			facade.outputQueue().enqueue(message.asReadOnlyBuffer());
		}

		public void sendTo(NadaUser recipient, ByteBuffer message) {
			recipient.send(prefix);
			recipient.send(message);
		}

		public String getNickName() {
			return nickName;
		}

		public void setNickName(String nickName) {
			this.nickName = nickName;

			String prefixStr = "[" + nickName + "] ";

			prefix = ByteBuffer.wrap(prefixStr.getBytes());
		}
	}
}
