/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.samples.sessionclosed;

import de.root1.simon.samples.helloworld.server.*;
import java.io.IOException;
import java.net.UnknownHostException;

import de.root1.simon.Registry;
import de.root1.simon.Simon;
import de.root1.simon.exceptions.NameBindingException;

public class Server {

   public static void main(String[] args)
   		throws UnknownHostException, IOException, NameBindingException {

      // create the serverobject
      ServerInterfaceImpl serverImpl = new ServerInterfaceImpl();

      // create the server's registry ...
      Registry registry = Simon.createRegistry(22222);
      registry.start();

      // ... where we can bind the serverobject to
      registry.bind("server", serverImpl);

      System.out.println("Server up and running!");

      // some mechanism to shutdown the server should be placed here
      // this should include the following command:
      // registry.unbind("server");
   }
}
