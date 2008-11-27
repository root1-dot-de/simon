package de.root1.simon.experiments;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;

import de.root1.simon.Dispatcher;
import de.root1.simon.LookupTable;
import de.root1.simon.Simon;
import de.root1.simon.codec.base.SimonProtocolCodecFactory;
import de.root1.simon.utils.Utils;

public class UdpTest {
	
	public static void main(String[] args) throws IOException {
		IoAcceptor acceptor = new NioDatagramAcceptor();
		
		Dispatcher dispatcher = new Dispatcher("",new LookupTable(),Executors.newCachedThreadPool());
		
		SimonProtocolCodecFactory protocolFactory = null;
		try {
			protocolFactory = Utils.getFactoryInstance(Simon.SIMON_STD_PROTOCOL_CODEC_FACTORY);
		} catch (ClassNotFoundException e) {
			// already proved
		} catch (InstantiationException e) {
			// already proved
		} catch (IllegalAccessException e) {
			// already proved
		}
		protocolFactory.setup(true);
		acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(protocolFactory));
        
		InetAddress address = InetAddress.getLocalHost();
		int port = 2222;
		
		acceptor.setHandler(dispatcher);
        acceptor.getSessionConfig().setIdleTime( IdleStatus.BOTH_IDLE, 10 );
        acceptor.bind(new InetSocketAddress(address, port));
	}

}
