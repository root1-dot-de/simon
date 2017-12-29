/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.samples.rawchannel2.client;

import de.root1.simon.Lookup;
import de.root1.simon.Simon;
import de.root1.simon.exceptions.EstablishConnectionFailed;
import de.root1.simon.exceptions.LookupFailedException;
import de.root1.simon.exceptions.SimonRemoteException;
import de.root1.simon.samples.rawchannel2.shared.MyServer;
import java.io.IOException;

/**
 *
 * @author ACHR
 */
public class Client {

    public static void main(String[] args) {
        System.out.println("Client started...");
        try {
            String filename = "C:/TheKnack.mpg";


            ClientCallbackImpl clientCallback = new ClientCallbackImpl();
            System.out.println("Doing lookup ...");
            Lookup nameLookup = Simon.createNameLookup("localhost", 22222);
            
            MyServer myServer = (MyServer) nameLookup.lookup("myServer");
            System.out.println("Requesting file: "+filename);
            myServer.requestFile(clientCallback, filename);
            System.out.println("File request completed, releasing remote object");
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
