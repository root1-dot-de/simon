/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test.clientcallback;

/**
 *
 * @author ACHR
 */
public class ClientCallbackImpl implements ClientCallback {

    @Override
    public void sayHello() {
        System.out.println("Hello");
    }

    @Override
    public void sayObjectHello(Object o) {
        
    }

}
