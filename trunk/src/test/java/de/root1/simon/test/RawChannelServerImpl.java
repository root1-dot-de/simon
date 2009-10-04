/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test;

import de.root1.simon.RawChannelDataListener;
import de.root1.simon.Simon;
import de.root1.simon.exceptions.SimonRemoteException;
import java.nio.ByteBuffer;

/**
 *
 * @author achristian
 */
public class RawChannelServerImpl implements RawChannelServer {

    public int openFileChannel(String filename) throws SimonRemoteException {
        System.out.println("openFileChannel("+filename+")");
        return Simon.prepareRawChannel(new RawChannelDataListener(){

            public void write(ByteBuffer data) {
                System.out.println("write(): "+data);
                data.flip();
                System.out.println(data.get());
                System.out.println(data.get());
                System.out.println(data.get());
                System.out.println(data.get());
            }

            public void close() {
                System.out.println("close()");
            }

        },this);
        
    }

}
