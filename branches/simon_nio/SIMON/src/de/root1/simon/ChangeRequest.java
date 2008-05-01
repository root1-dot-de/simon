package de.root1.simon;
import java.nio.channels.SocketChannel;

import de.root1.simon.utils.Utils;

public class ChangeRequest {
	public static final int REGISTER = 1;
	public static final int CHANGEOPS = 2;
	
	public SocketChannel socket;
	public int type;
	public int ops;
	
	public ChangeRequest(SocketChannel socket, int type, int ops) {
		Utils.logger.finer("begin");
		this.socket = socket;
		this.type = type;
		this.ops = ops;
		Utils.logger.finer("end");
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[socket=");
		sb.append(socket);
		sb.append("|type=");
		sb.append(type);
		sb.append("|ops=");
		sb.append(ops);
		sb.append("]");
		return sb.toString();
	}
}
