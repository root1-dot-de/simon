/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.root1.simon.test.phantomref;

import de.root1.simon.annotation.SimonRemote;

/**
 *
 * @author achristian
 */
@SimonRemote(value={ServerCallback.class})
public class ServerCallbackImpl implements ServerCallback {

    @Override
    public void sayHelloToServer() {
        System.out.println("Client said hello to us (server)");
    }
    
}
