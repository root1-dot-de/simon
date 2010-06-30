/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.samples.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import de.root1.simon.Simon;
import de.root1.simon.exceptions.LookupFailedException;
import de.root1.simon.samples.helloworld.client.ClientCallbackImpl;
import de.root1.simon.samples.helloworld.shared.ServerInterface;
import de.root1.simon.ssl.DefaultSslContextFactory;


 public class Client {

     public static void main(String[] args) throws IOException, LookupFailedException, UnrecoverableKeyException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException, CertificateException {

         // create a callback object
         ClientCallbackImpl clientCallbackImpl = new ClientCallbackImpl();

         // 'lookup' the server object via SSL
         ServerInterface server = (ServerInterface) Simon.lookup(new DefaultSslContextFactory("path_to_keystore/.clientkeystore","MyKeyStorePass"), null, InetAddress.getByName("127.0.0.1"), 22222, "server");

         // use the serverobject as it would exist on your local machine
         server.login(clientCallbackImpl);
         
         // do some more stuff
         // ...

           // and finally 'release' the serverobject to release to connection to the server
         Simon.release(server);
    }
}
