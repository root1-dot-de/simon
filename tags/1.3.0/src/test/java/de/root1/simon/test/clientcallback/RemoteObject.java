/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test.clientcallback;

/**
 *
 * @author achristian
 */
public interface RemoteObject {

    public void setCallback(ClientCallback clientCallback);
    public ClientCallback getCallback();
    public void sendCallbackViaCallback();
    
    public RemoteObject getRemoteObject();
    
    public Session getSessionObject();
    public void setSessionObject(Session s);
    
    public boolean testEquals(ClientCallback clientCallback);
    
}
