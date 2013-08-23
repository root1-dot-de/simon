/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.root1.simon.test.unreferenced;

// mark this class as a remote class and export all methods known in ClientCallbackInterface

import de.root1.simon.SimonUnreferenced;
import de.root1.simon.annotation.SimonRemote;

@SimonRemote(value = {ClientCallbackInterface.class})
public class ClientCallbackImpl implements ClientCallbackInterface, SimonUnreferenced {

    private static final long serialVersionUID = 1L;

    @Override
    public void callback(String text) {

        System.out.println("This message was received from the server: " + text);

    }

    @Override
    public void unreferenced() {
        System.out.println("unreferenced: "+this);
    }
}