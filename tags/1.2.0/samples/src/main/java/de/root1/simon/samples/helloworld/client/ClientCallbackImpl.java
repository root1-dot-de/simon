/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.samples.helloworld.client;

import de.root1.simon.annotation.SimonRemote;
import de.root1.simon.samples.helloworld.shared.ClientCallbackInterface;

@SimonRemote(value={ClientCallbackInterface.class})
public class ClientCallbackImpl implements ClientCallbackInterface {

   private static final long serialVersionUID = 1L;

   public void callback(String text) {

      System.out.println("This message was received from the server: "+text);

   }
} 
