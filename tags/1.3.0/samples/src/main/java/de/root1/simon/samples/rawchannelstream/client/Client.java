package de.root1.simon.samples.rawchannelstream.client;

import de.root1.simon.Lookup;
import de.root1.simon.Simon;
import de.root1.simon.exceptions.EstablishConnectionFailed;
import de.root1.simon.exceptions.LookupFailedException;
import de.root1.simon.exceptions.SimonRemoteException;
import de.root1.simon.samples.rawchannelstream.shared.MyServer;
import java.io.IOException;

public class Client {

    public static void main(String[] args) {
        System.out.println("Client started...");
        try {

            ClientCallbackImpl clientCallback = new ClientCallbackImpl();
            System.out.println("Doing lookup ...");
            Lookup nameLookup = Simon.createNameLookup("localhost");
            
            MyServer myServer = (MyServer) nameLookup.lookup("myServer");
            
            System.out.println("Requesting stream ...");
            myServer.requestStream(clientCallback);
            
            System.out.println("Stream request completed, releasing remote object");
            nameLookup.release(myServer);

        } catch (SimonRemoteException ex) {
            ex.printStackTrace();
        } catch (LookupFailedException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (EstablishConnectionFailed ex) {
            ex.printStackTrace();
        }
        System.out.println("Client terminated...");
    }

}
