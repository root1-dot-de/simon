/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test.rawchannel;

import de.root1.simon.SimonRemote;
import de.root1.simon.exceptions.SimonRemoteException;

/**
 *
 * @author achristian
 */
interface RawChannelServer extends SimonRemote {

    public int openFileChannel(String filename) throws SimonRemoteException;

    public byte[] getFileBytes(String filename) throws SimonRemoteException;

}
