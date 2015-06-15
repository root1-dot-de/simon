/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test.phantomref;

import de.root1.simon.test.clientcallback.*;


/**
 *
 * @author ACHR
 */
public interface ClientCallback {

    public void sayHello();
    
    public void sayObjectHello(Object o);
}
