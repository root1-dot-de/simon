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
//@SimonRemote({RemoteObject1.class, RemoteObject2.class})
//@SimonRemote(RemoteObject2.class)
public class RemoteObjectImpl implements RemoteObject1, RemoteObject2 {

    public void myRemoteMethod1() {
        System.out.println("myRemoteMethod1() called!");
    }

    public void myRemoteMethod2() {
        System.out.println("myRemoteMethod2() called!");
    }

}
