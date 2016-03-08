/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.root1.simon.samples.sessionclosed;

import de.root1.simon.ClosedListener;
import de.root1.simon.samples.helloworld.client.*;
import de.root1.simon.Lookup;
import de.root1.simon.exceptions.EstablishConnectionFailed;
import java.io.IOException;

import de.root1.simon.Simon;
import de.root1.simon.exceptions.LookupFailedException;
import de.root1.simon.samples.helloworld.shared.ServerInterface;

public class Client {
    
    public static class MyClosedListsner implements ClosedListener {
        
        volatile boolean isClosed = false;

        @Override
        public void closed() {
            System.out.println("CLOSED!");
            isClosed=true;
        }
    }
    
    public static class MyUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            System.err.println("Exception occured: Thread="+t+". Exception="+e);
            System.err.flush();
        }
        
    }

    public static void main(String[] args) throws IOException, LookupFailedException, EstablishConnectionFailed {

        Thread.currentThread().setUncaughtExceptionHandler(new MyUncaughtExceptionHandler());
        
        // create a callback object
        ClientCallbackImpl clientCallbackImpl = new ClientCallbackImpl();
        MyClosedListsner myClosedListener = new MyClosedListsner();
        
        // 'lookup' the server object
        Lookup nameLookup = Simon.createNameLookup("127.0.0.1", 22222);
        ServerInterface server = (ServerInterface) nameLookup.lookup("server");
        nameLookup.addClosedListener(server, myClosedListener);

        while (!myClosedListener.isClosed) {
            // use the serverobject as it would exist on your local machine
            server.login(clientCallbackImpl);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
        }

        // do some more stuff
        // ...

        // and finally 'release' the serverobject to release to connection to the server
        nameLookup.release(server);
    }

}
