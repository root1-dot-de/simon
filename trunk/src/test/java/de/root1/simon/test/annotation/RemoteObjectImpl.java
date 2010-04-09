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
@SimonRemote({RemoteObject1.class, RemoteObject2.class})
public class RemoteObjectImpl implements RemoteObject1, RemoteObject2, RemoteObject3 {

    public void myRemoteMethod1() {
        System.out.println("myRemoteMethod1() called!");
    }

    public void myRemoteMethod2() {
        System.out.println("myRemoteMethod2() called!");
    }

    public void myRemoteMethod3() {
        System.out.println("myRemoteMethod3() called!");
    }

}
