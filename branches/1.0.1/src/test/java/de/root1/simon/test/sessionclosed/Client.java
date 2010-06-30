/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.root1.simon.test.sessionclosed;

import de.root1.simon.Simon;
import de.root1.simon.exceptions.SimonRemoteException;
import de.root1.simon.utils.Utils;

public class Client {

    public static void main(String[] args) throws Exception {
        Utils.DEBUG = true;
        Server server = (Server) Simon.lookup("127.0.0.1", 22222, "server");
        for (;;) {
            try {
                System.out.println("hashCode="+server.hashCode());
                Thread.sleep(1000);
            } catch (Throwable x) {
                x.printStackTrace();
                System.out.println("Reconnecting ....");
                if (server!=null) {
                    Simon.release(server);
                }
                server = (Server) Simon.lookup("127.0.0.1", 22222, "server");
                System.out.println("Reconnecting .... *done*");
            }
        }
    }
}
