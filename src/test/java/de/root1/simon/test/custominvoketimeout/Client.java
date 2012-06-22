/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.root1.simon.test.custominvoketimeout;

import de.root1.simon.Lookup;
import de.root1.simon.Simon;
import de.root1.simon.exceptions.EstablishConnectionFailed;
import de.root1.simon.exceptions.LookupFailedException;
import java.net.UnknownHostException;

/**
 *
 * @author achristian
 */
public class Client {
    
    public static void main(String[] args) throws UnknownHostException, LookupFailedException, EstablishConnectionFailed {
        
        
        ClientCallbackImpl cc = new ClientCallbackImpl();
        
        Lookup lookup = Simon.createNameLookup("localhost");
        Server server = (Server) lookup.lookup("server");
        
        server.doSomething(cc);
        
        lookup.release(server);
    }
    
}
