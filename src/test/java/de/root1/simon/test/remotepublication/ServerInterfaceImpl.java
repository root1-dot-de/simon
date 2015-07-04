/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.root1.simon.test.remotepublication;

import de.root1.simon.annotation.SimonRemote;

/**
 *
 * @author achristian
 */
@SimonRemote(value = ServerInterface.class)
public class ServerInterfaceImpl implements ServerInterface {

    @Override
    public void sayHello() {
        System.out.println("Saying Hello");
    }
    
}
