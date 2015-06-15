/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.root1.simon.samples.ssl;

import de.root1.simon.Lookup;
import de.root1.simon.exceptions.EstablishConnectionFailed;
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

    public static void main(String[] args) throws IOException, LookupFailedException, UnrecoverableKeyException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException, CertificateException, EstablishConnectionFailed {

        // create a callback object
        ClientCallbackImpl clientCallbackImpl = new ClientCallbackImpl();
        
        // 'lookup' the server object via SSL
        Lookup nameLookup = Simon.createNameLookup(InetAddress.getByName("127.0.0.1"), 22222);
        nameLookup.setSslContextFactory(new DefaultSslContextFactory("path_to_keystore/.clientkeystore", "MyKeyStorePass"));
        ServerInterface server = (ServerInterface) nameLookup.lookup("server");

        // use the serverobject as it would exist on your local machine
        server.login(clientCallbackImpl);

        // do some more stuff
        // ...

        // and finally 'release' the serverobject to release to connection to the server
        nameLookup.release(server);
    }
}
