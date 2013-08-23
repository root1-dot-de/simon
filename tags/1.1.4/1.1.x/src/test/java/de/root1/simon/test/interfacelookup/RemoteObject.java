/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test.interfacelookup;

import de.root1.simon.exceptions.SimonRemoteException;

/**
 *
 * @author achristian
 */
public interface RemoteObject {

    public void helloWorld() throws SimonRemoteException;
    public void helloWorldArg(String s) throws SimonRemoteException;

}
