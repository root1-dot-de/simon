/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test.clientcallback;

import de.root1.simon.SimonRemote;

/**
 *
 * @author achristian
 */
public interface RemoteObject extends SimonRemote {

    public void setCallback(ClientCallback clientCallback);

}
