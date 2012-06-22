/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.root1.simon.test.custominvoketimeout;

import de.root1.simon.Registry;
import de.root1.simon.Simon;
import de.root1.simon.annotation.SimonRemote;
import de.root1.simon.exceptions.InvokeTimeoutException;
import de.root1.simon.exceptions.NameBindingException;
import de.root1.simon.exceptions.SimonRemoteException;
import java.io.IOException;
import java.net.UnknownHostException;

/**
 *
 * @author achristian
 */
@SimonRemote(value={Server.class})
public class ServerImpl implements Server {

    @Override
    public void doSomething(ClientCallback cc) {
        
        try {
            cc.confirm("Hallo Welt");
            System.out.println("doing something");
        } catch (InvokeTimeoutException ex) {
            ex.printStackTrace();
            System.out.println("doing nothing, because client was too busy to answer my callback");
        }
    }
    
    public static void main(String[] args) throws UnknownHostException, IOException, NameBindingException, NoSuchMethodException {
        System.setProperty("de.root1.simon.debug", "true");
        Simon.setCustomInvokeTimeout(ClientCallback.class.getDeclaredMethod("confirm", new Class[]{String.class}), 1000);
        
        ServerImpl si = new ServerImpl();
        Registry registry = Simon.createRegistry();
        registry.bind("server", si);
        System.out.println("Server running ...");
    }
    
}
