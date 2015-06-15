/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.samples.rawchannel2.shared;


/**
 *
 * @author ACHR
 */
public interface RawChannelFromServerToClient {

    public static final String BIND_NAME = "RawChannelFileTransfer";

    public int openFileChannel(String filename);

    public byte[] getFileBytes(String filename);

}
