/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test;

import de.root1.simon.exceptions.SimonRemoteException;

/**
 *
 * @author achristian
 */
public class RemoteObjectImpl implements RemoteObject {

    public void helloWorld() throws SimonRemoteException {
        System.out.println("Hello World");
    }

}
