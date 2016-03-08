/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.root1.simon.test.custominvoketimeout;

import de.root1.simon.annotation.SimonRemote;


/**
 *
 * @author achristian
 */
@SimonRemote(value={ClientCallback.class})
public class ClientCallbackImpl implements ClientCallback {

    @Override
    public boolean confirm(String msg) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        System.out.println("msg: "+msg);
        return true;
    }
    
}
