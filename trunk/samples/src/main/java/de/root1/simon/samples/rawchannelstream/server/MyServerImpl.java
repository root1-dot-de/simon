package de.root1.simon.samples.rawchannelstream.server;

import de.root1.simon.RawChannel;
import de.root1.simon.RawChannelOutputStream;
import de.root1.simon.Simon;
import de.root1.simon.annotation.SimonRemote;
import de.root1.simon.samples.rawchannelstream.shared.ClientCallback;
import de.root1.simon.samples.rawchannelstream.shared.MyServer;
import java.io.IOException;

@SimonRemote(value = {MyServer.class})
public class MyServerImpl implements MyServer {

    @Override
    public void requestStream(ClientCallback clientCallback) {

        int channelToken = clientCallback.establishStream();
        RawChannel rawChannel = Simon.openRawChannel(channelToken, clientCallback);
        RawChannelOutputStream rcos = new RawChannelOutputStream(rawChannel);
        try {
            rcos.write("Hello World via RawChannelStream".getBytes());
            rcos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
