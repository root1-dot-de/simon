/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.samples.loginsessionfactory.client;

import de.root1.simon.exceptions.SimonRemoteException;
import de.root1.simon.samples.loginsessionfactory.shared.LoginFailedException;
import de.root1.simon.samples.loginsessionfactory.shared.SessionInterface;
import java.io.IOException;

import de.root1.simon.Simon;
import de.root1.simon.exceptions.LookupFailedException;
import de.root1.simon.samples.helloworld.shared.ServerInterface;
import de.root1.simon.samples.loginsessionfactory.shared.LoginInterface;

public class Client {

   public static void main(String[] args) throws IOException, LookupFailedException {

      // 'lookup' the server object
      LoginInterface server = (LoginInterface) Simon.lookup("127.0.0.1", 22222, "server");
        try {
            // use the serverobject as it would exist on your local machine
            SessionInterface session = server.login("myAuthorizedUser", "myAuthorizedPass");
            
            session.sessionMethodA();
            session.sessionMethodB();
            session.sessionMethodC();

        } catch (SimonRemoteException ex) {
            ex.printStackTrace();
        } catch (LoginFailedException ex) {
            ex.printStackTrace();
        }

      // do some more stuff
      // ...

      // and finally 'release' the serverobject to release to connection to the server
      Simon.release(server);
   }
}
