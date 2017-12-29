/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.samples.rawchannel.shared;

import de.root1.simon.exceptions.SimonRemoteException;

public interface RawChannelServer {

    public static final String BIND_NAME = "RawChannelFileTransfer";

    public int openFileChannel(String filename);

    public byte[] getFileBytes(String filename);

}
