/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test.clientcallback;

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
}
