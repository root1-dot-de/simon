/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test.clientcallback;

import de.root1.simon.annotation.SimonRemote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author achristian
 */
@SimonRemote(value={RemoteObject.class})
public class RemoteObjectImpl implements RemoteObject {
    
    Logger logger = LoggerFactory.getLogger(getClass());

    ClientCallback callback;
    private int i;
    
    @Override
    public void setCallback(ClientCallback clientCallback) {
        callback = clientCallback;
        clientCallback.sayHello();
    }

    @Override
    public ClientCallback getCallback() {
        return callback;
    }

    @Override
    public void sendCallbackViaCallback() {
        callback.sayObjectHello(callback);
    }
    
    @Override
    public boolean testEquals(ClientCallback clientCallback) {
        return clientCallback.equals(clientCallback);
    }
    
    @Override
    public boolean equals(Object obj) {
        logger.info("EQUALS ON "+this.getClass().getCanonicalName()+" CALLED! this="+this+" other="+obj);
        return super.equals(obj);
    }

    @Override
    public RemoteObject getRemoteObject() {
        return this;
    }

    @Override
    public Session getSessionObject() {
        int id = i++;
        logger.info("Created session#"+id);
        return new SessionImpl(id);
    }

    @Override
    public void setSessionObject(Session s) {
        logger.info("got Session #"+s.getId()+" back from client");
    }
}
