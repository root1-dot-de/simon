/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test;

import de.root1.simon.SimonRemote;
import de.root1.simon.exceptions.SimonRemoteException;

/**
 *
 * @author achristian
 */
public interface RemoteObject extends SimonRemote {

    public void helloWorld() throws SimonRemoteException;

}
