/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test.sourceaddress;

import de.root1.simon.Simon;
import de.root1.simon.annotation.SimonRemote;
import de.root1.simon.exceptions.SimonRemoteException;
import java.net.InetSocketAddress;

/**
 *
 * @author achristian
 */
@SimonRemote
public class RemoteObjectImpl implements RemoteObject {

    public String ping(ClientCallback ccb) throws SimonRemoteException {
        InetSocketAddress remoteInetSocketAddress = Simon.getRemoteInetSocketAddress(ccb);
        return remoteInetSocketAddress.toString();
    }

}
