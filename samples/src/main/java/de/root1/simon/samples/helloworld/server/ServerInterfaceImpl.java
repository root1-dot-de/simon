/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.samples.helloworld.server;

import de.root1.simon.Simon;
import de.root1.simon.annotation.SimonRemote;
import de.root1.simon.samples.helloworld.shared.ClientCallbackInterface;
import de.root1.simon.samples.helloworld.shared.ServerInterface;

@SimonRemote(value={ServerInterface.class})
public class ServerInterfaceImpl implements ServerInterface {

   private static final long serialVersionUID = 1L;

   public void login(ClientCallbackInterface clientCallback) {
 
      clientCallback.callback("This is the callback. " +
         "Your address is "+
         Simon.getRemoteInetSocketAddress(clientCallback).getAddress()+" "+
         "and your are connected from port "+
         Simon.getRemoteInetSocketAddress(clientCallback).getPort());

   }
}
