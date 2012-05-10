/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.samples.rawchannel2.shared;

import de.root1.simon.SimonRemote;
import de.root1.simon.exceptions.SimonRemoteException;

/**
 *
 * @author ACHR
 */
public interface RawChannelFromServerToClient extends SimonRemote {

    public static final String BIND_NAME = "RawChannelFileTransfer";

    public int openFileChannel(String filename) throws SimonRemoteException;

    public byte[] getFileBytes(String filename) throws SimonRemoteException;

}
