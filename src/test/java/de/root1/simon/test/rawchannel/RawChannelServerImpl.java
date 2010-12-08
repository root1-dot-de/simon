/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test.rawchannel;

import de.root1.simon.Simon;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author achristian
 */
@de.root1.simon.annotation.SimonRemote
public class RawChannelServerImpl implements RawChannelServer {

    @Override
    public int openFileChannel(String filename){
        int token = Simon.prepareRawChannel(new FileReceiver(filename),this);
        System.out.println("-> opened raw data channel on server side for file '"+filename+"'. token is: "+token);
        return token;
        
    }

    @Override
    public byte[] getFileBytes(String filename){
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
