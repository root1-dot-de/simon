/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.root1.simon.test.phantomref;

import java.io.Serializable;

/**
 *
 * @author achristian
 */
public interface ServerCallback extends Serializable {
    
    public void sayHelloToServer();
    
}
