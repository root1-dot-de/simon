/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test.phantomref;

import de.root1.simon.annotation.SimonRemote;

/**
 *
 * @author achristian
 */
@SimonRemote(value={RemoteObject.class})
public class RemoteObjectImpl implements RemoteObject {

    ClientCallback callback;
    
    @Override
    public void setCallback(ClientCallback clientCallback) {
        callback = clientCallback;
        if (callback!=null) {
            clientCallback.sayHello();
        }
        System.gc();
        System.gc();
        System.gc();
        System.gc();
        System.gc();
        System.gc();
        System.gc();
        System.gc();
        System.gc();
        System.gc();
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
        System.out.println("EQUALS ON "+this.getClass().getCanonicalName()+" CALLED! this="+this+" other="+obj);
        return super.equals(obj);
    }

    @Override
    public RemoteObject getRemoteObject() {
        return this;
    }

    @Override
    public ServerCallback getServerCallback() {
        return new ServerCallbackImpl();
    }
}
