/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test.annotation;

import de.root1.simon.Remote;

/**
 *
 * @author achristian
 */
@Remote
public class RemoteObjectImpl implements RemoteObject {

    public void myRemoteMethod() {
        System.out.println("myRemoteMethod() called!");
    }

}
