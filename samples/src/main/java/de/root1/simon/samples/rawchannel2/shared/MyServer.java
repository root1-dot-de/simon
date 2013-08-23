/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.samples.rawchannel2.shared;

import de.root1.simon.SimonRemote;
import de.root1.simon.exceptions.SimonRemoteException;
import de.root1.simon.samples.rawchannel2.client.ClientCallbackImpl;

/**
 *
 * @author ACHR
 */
public interface MyServer extends SimonRemote {

    public void doSomething() throws SimonRemoteException;

     public void requestFile(ClientCallback clientCallback, String filename) throws SimonRemoteException;

}
