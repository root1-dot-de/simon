/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.root1.simon.test.clientcallback;

import de.root1.simon.annotation.SimonRemote;
import java.io.Serializable;

/**
 *
 * @author achristian
 */
@SimonRemote(value={Session.class})
public class SessionImpl implements Session, Serializable {

    int i;
    
    SessionImpl(int i) {
        this.i=i;
    }

    @Override
    public void saySessionHello(String msg) {
        System.out.println("Session#"+i+": Hello "+msg);
    }

    @Override
    public int getId() {
        return i;
    }
    
}
