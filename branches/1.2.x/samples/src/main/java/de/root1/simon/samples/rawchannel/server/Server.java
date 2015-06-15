/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.samples.rawchannel.server;

import de.root1.simon.Registry;
import de.root1.simon.Simon;
import de.root1.simon.exceptions.NameBindingException;
import de.root1.simon.samples.rawchannel.shared.RawChannelServer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Server {

    public static void main(String[] args) {
        
        try {

            Registry registry = Simon.createRegistry(InetAddress.getByName("0.0.0.0"), 2000);
            registry.start();

            RawChannelServerImpl rcsi = new RawChannelServerImpl() ;
            registry.bind( RawChannelServer.BIND_NAME, rcsi);

            // Server is now running. If you whish to shutdown, call this lines:
            //registry.stop();
            //registry.unbind(RawChannelServer.BIND_NAME);

        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (NameBindingException ex) {
            ex.printStackTrace();
        }

    }

}
