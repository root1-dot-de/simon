/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.samples.loginsessionfactory.shared;

import de.root1.simon.SimonRemote;
import de.root1.simon.exceptions.SimonRemoteException;

public interface LoginInterface extends SimonRemote {

   public SessionInterface login(String user, String pass) throws SimonRemoteException, LoginFailedException;

}
