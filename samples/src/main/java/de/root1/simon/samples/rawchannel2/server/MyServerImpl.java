/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.samples.rawchannel2.server;

import de.root1.simon.RawChannel;
import de.root1.simon.Simon;
import de.root1.simon.annotation.SimonRemote;
import de.root1.simon.exceptions.SimonRemoteException;
import de.root1.simon.exceptions.RawChannelException;
import de.root1.simon.samples.rawchannel2.shared.ClientCallback;
import de.root1.simon.samples.rawchannel2.shared.MyServer;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 *
 * @author ACHR
 */
@SimonRemote(value={MyServer.class})
public class MyServerImpl implements MyServer {

    public void doSomething() {
        System.out.println("Doing something on server ...");
    }

   public void requestFile(ClientCallback clientCallback, String filename) {
        
        int channelToken = clientCallback.prepareFileTransfer(filename+".received");
        RawChannel rawChannel = Simon.openRawChannel(channelToken, clientCallback);
        try {
            FileChannel fileChannel = new FileInputStream(filename).getChannel();

            // we send the file in 512byte packages through the RawChannel
            ByteBuffer data = ByteBuffer.allocate(8192);
            while (fileChannel.read(data) != -1) {
                rawChannel.write(data);
                data.clear();
            }

            // all data written. Now we can close the FileChannel
            fileChannel.close();

            // ... and also th RawChannel
            rawChannel.close();

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (RawChannelException ex) {
            ex.printStackTrace();
        }
    }

}
