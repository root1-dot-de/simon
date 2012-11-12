/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test.phantomref;

import de.root1.simon.test.clientcallback.*;
import de.root1.simon.annotation.SimonRemote;
import java.io.Serializable;

/**
 *
 * @author ACHR
 */
@SimonRemote(value={ClientCallback.class})
public class ClientCallbackImpl implements ClientCallback, Serializable {

    @Override
    public void sayHello() {
        System.out.println("Hello from Callback");
    }

    @Override
    public void sayObjectHello(Object o) {
        
    }

}
