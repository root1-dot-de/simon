/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.samples.loginsessionfactory.shared;

import de.root1.simon.SimonRemote;
import de.root1.simon.exceptions.SimonRemoteException;

/**
 *
 * @author ACHR
 */
public interface SessionInterface extends SimonRemote {

    public void sessionMethodA() throws SimonRemoteException;
    public void sessionMethodB() throws SimonRemoteException;
    public void sessionMethodC() throws SimonRemoteException;

}
