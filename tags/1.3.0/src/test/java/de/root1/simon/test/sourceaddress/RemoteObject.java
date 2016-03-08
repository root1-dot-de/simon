/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test.sourceaddress;

import de.root1.simon.exceptions.SimonRemoteException;

/**
 *
 * @author achristian
 */
public interface RemoteObject {

    public String ping(ClientCallback ccb) throws SimonRemoteException;

}
