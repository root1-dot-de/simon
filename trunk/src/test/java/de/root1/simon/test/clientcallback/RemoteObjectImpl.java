/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test.clientcallback;

import de.root1.simon.exceptions.SimonRemoteException;

/**
 *
 * @author achristian
 */
public class RemoteObjectImpl implements RemoteObject {

    @Override
    public void setCallback(ClientCallback clientCallback) {
        clientCallback.sayHello();
    }
}
