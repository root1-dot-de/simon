/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.samples.rawchannel.server;

import de.root1.simon.Simon;
import de.root1.simon.annotation.SimonRemote;
import de.root1.simon.exceptions.SimonRemoteException;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import de.root1.simon.samples.rawchannel.shared.RawChannelServer;

@SimonRemote(value={RawChannelServer.class})
public class RawChannelServerImpl implements RawChannelServer {

    public int openFileChannel(String filename) throws SimonRemoteException {
        int token = Simon.prepareRawChannel(new FileReceiver(filename),this);
        return token;      
    }

    public byte[] getFileBytes(String filename) {

        File f = new File(filename);

        byte[] data = new byte[(int)f.length()];

        DataInputStream dis;
        try {
            dis = new DataInputStream(new FileInputStream(f));
            dis.readFully(data);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return data;
    }

}
