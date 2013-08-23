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
public interface ClientCallback extends SimonRemote {

    public int prepareFileTransfer(String fileName) throws SimonRemoteException;

}
