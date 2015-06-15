/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.samples.rawchannel2.client;

import de.root1.simon.Simon;
import de.root1.simon.annotation.SimonRemote;
import de.root1.simon.exceptions.SimonRemoteException;
import de.root1.simon.samples.rawchannel2.shared.ClientCallback;

/**
 *
 * @author ACHR
 */
@SimonRemote(value={ClientCallback.class})
public class ClientCallbackImpl implements ClientCallback {

    // register the file receiver and return the channel token
    public int prepareFileTransfer(final String fileName) {
        return Simon.prepareRawChannel(new FileReceiver(fileName), this);
    }

}
