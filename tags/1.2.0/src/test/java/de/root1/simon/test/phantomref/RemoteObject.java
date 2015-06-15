/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test.phantomref;

import de.root1.simon.test.clientcallback.*;

/**
 *
 * @author achristian
 */
public interface RemoteObject {

    public void setCallback(ClientCallback clientCallback);
    
    /**
     * Returns the callback which the client has set via setCallback()
     * @return client's callback object
     */
    public ClientCallback getCallback();
    
    /**
     * Invoke the client's callback and give the callback itself as a parameter
     */
    public void sendCallbackViaCallback();
    
    /**
     * Returns this remote object instance
     * @return this
     */
    public RemoteObject getRemoteObject();
    
    public boolean testEquals(ClientCallback clientCallback);
    
    public ServerCallback getServerCallback();
    
}
