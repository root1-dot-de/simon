/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test.interfacelookup;

import de.root1.simon.annotation.SimonRemote;
import de.root1.simon.exceptions.SimonRemoteException;

/**
 *
 * @author achristian
 */
@SimonRemote
public class RemoteObjectImpl implements RemoteObject {

    public void helloWorld() throws SimonRemoteException {
//        System.out.println("Hello World");
    }

    public void helloWorldArg(String s) throws SimonRemoteException {
//        System.out.println("Hello WorldArg ["+s+"]");
    }

}
