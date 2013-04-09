/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.root1.simon.samples.rawchannelstream.server;

import de.root1.simon.RawChannel;
import de.root1.simon.RawChannelOutputStream;
import de.root1.simon.Simon;
import de.root1.simon.annotation.SimonRemote;
import de.root1.simon.samples.rawchannelstream.shared.ClientCallback;
import de.root1.simon.samples.rawchannelstream.shared.MyServer;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ACHR
 */
@SimonRemote(value = {MyServer.class})
public class MyServerImpl implements MyServer {

    @Override
    public void doSomething() {
        System.out.println("Doing something on server ...");
    }

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
