/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test.annotation;

import de.root1.simon.annotation.SimonRemote;

/**
 *
 * @author achristian
 */
@SimonRemote
public class RemoteObjectImpl implements RemoteObject {

    public void myRemoteMethod() {
        System.out.println("myRemoteMethod() called!");
    }

}
