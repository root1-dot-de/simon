/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.samples.helloworld.shared;

import de.root1.simon.SimonRemote;
import de.root1.simon.exceptions.SimonRemoteException;

public interface ClientCallbackInterface extends SimonRemote {

   public void callback(String text) throws SimonRemoteException;

} 
