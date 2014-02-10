/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.samples.jasypt.server;

import de.root1.simon.Registry;
import de.root1.simon.Simon;
import de.root1.simon.annotation.SimonRemote;
import de.root1.simon.exceptions.NameBindingException;
import de.root1.simon.jasypt.JasyptSimonPBE;
import de.root1.simon.samples.jasypt.shared.SimonJasyptServerInterface;
import java.io.IOException;

/**
 *
 * @author achristian
 */
@SimonRemote(value = {SimonJasyptServerInterface.class})
public class SimonJasyptServer implements SimonJasyptServerInterface {

    @Override
    public String sayHelloTo(String name) {
        return "Hello "+name;
    }
    
    public static void main(String[] args) throws IOException, NameBindingException {
        Registry registry = Simon.createRegistry();
        registry.setCustomEncryption(new JasyptSimonPBE("password"));
        registry.start();
        registry.bind(SimonJasyptServerInterface.NAME, new SimonJasyptServer());
        System.out.println("running ...");
    }
    
}
