/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test.rawchannel;

import de.root1.simon.Simon;
import de.root1.simon.exceptions.SimonRemoteException;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author achristian
 */
public class RawChannelServerImpl implements RawChannelServer {

    public int openFileChannel(String filename) throws SimonRemoteException {
        int token = Simon.prepareRawChannel(new FileReceiver(filename),this);
        System.out.println("-> opened raw data channel on server side for file '"+filename+"'. token is: "+token);
        return token;
        
    }

    public byte[] getFileBytes(String filename) throws SimonRemoteException {
        System.out.println("-> transfering received file back to client.");
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
