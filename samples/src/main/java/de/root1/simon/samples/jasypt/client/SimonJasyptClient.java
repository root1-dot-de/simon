/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.samples.jasypt.client;

import de.root1.simon.Lookup;
import de.root1.simon.NameLookup;
import de.root1.simon.Simon;
import de.root1.simon.exceptions.EstablishConnectionFailed;
import de.root1.simon.exceptions.LookupFailedException;
import de.root1.simon.jasypt.JasyptSimonPBE;
import de.root1.simon.samples.jasypt.server.SimonJasyptServer;
import de.root1.simon.samples.jasypt.shared.SimonJasyptServerInterface;
import java.net.UnknownHostException;

/**
 *
 * @author achristian
 */
public class SimonJasyptClient {
    
    public static void main(String[] args) throws UnknownHostException, LookupFailedException, EstablishConnectionFailed {

        Lookup lookup = Simon.createNameLookup("localhost");
        lookup.setCustomEncryption(new JasyptSimonPBE("password"));
        
        SimonJasyptServerInterface server = (SimonJasyptServerInterface) lookup.lookup(SimonJasyptServerInterface.NAME);
        
        String msg = server.sayHelloTo("Max Mustermann");
        System.out.println("msg: "+msg);
        
        lookup.release(server);
    }
    
}
