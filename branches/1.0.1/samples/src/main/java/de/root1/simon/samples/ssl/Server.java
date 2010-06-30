/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.samples.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import de.root1.simon.Registry;
import de.root1.simon.Simon;
import de.root1.simon.exceptions.NameBindingException;
import de.root1.simon.samples.helloworld.server.ServerInterfaceImpl;
import de.root1.simon.ssl.DefaultSslContextFactory;

public class Server {

public static void main(String[] args)
    throws UnknownHostException, IOException, NameBindingException, UnrecoverableKeyException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException, CertificateException {

        // create the serverobject
        ServerInterfaceImpl serverImpl = new ServerInterfaceImpl();

       // create a SSL enabled server's registry ...
       Registry registry = Simon.createRegistry(new DefaultSslContextFactory("path_to_keystore/.serverkeystore","MyKeyStorePass"), InetAddress.getByName("0.0.0.0"), 22222);

       // ... where we can bind the serverobject to
       registry.bind("server", serverImpl);

       System.out.println("Server up and running!");

       // some mechanism to shutdown the server should be placed here
       // this should include the following command:
       // registry.unbind("server");
    }
}
