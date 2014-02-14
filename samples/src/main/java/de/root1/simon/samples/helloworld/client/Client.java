/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.root1.simon.samples.helloworld.client;

import de.root1.simon.Lookup;
import de.root1.simon.exceptions.EstablishConnectionFailed;
import java.io.IOException;

import de.root1.simon.Simon;
import de.root1.simon.exceptions.LookupFailedException;
import de.root1.simon.samples.helloworld.shared.ServerInterface;

public class Client {

    public static void main(String[] args) throws IOException, LookupFailedException, EstablishConnectionFailed {

        // create a callback object
        ClientCallbackImpl clientCallbackImpl = new ClientCallbackImpl();
        
        // 'lookup' the server object
        Lookup nameLookup = Simon.createNameLookup("127.0.0.1", 22222);
        
        ServerInterface server = (ServerInterface) nameLookup.lookup("server");

        // use the serverobject as it would exist on your local machine
        server.login(clientCallbackImpl);

        // do some more stuff
        // ...

        // and finally 'release' the serverobject to release to connection to the server
        nameLookup.release(server);
    }
}
