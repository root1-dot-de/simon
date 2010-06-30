/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.root1.simon.test.sessionclosed;

import de.root1.simon.Registry;
import de.root1.simon.Simon;
import de.root1.simon.exceptions.SimonRemoteException;

public class ServerImpl implements Server {

    static private class Client implements Runnable {

        public void run() {
            try {
                Server server = null;
                for (;;) {
                    try {
                        server = (Server) Simon.lookup("127.0.0.1", 22222, "server");
                        server.foo();
                    } catch (final Exception x) {
                        System.err.println("client reported " + x);
                        if (server != null) {
                            try {
                                Simon.release(server);
                            } catch (final Exception xx) {
                                System.err.println("relleas()" + xx);
                            } finally {
                                server = null;
                            }
                        }
                    }
                    Thread.sleep(1000);
                }
            } catch (final Exception x) {
                System.err.println("client ran out with " + x);
            }
        }
    }

    public static void main(String[] args) throws Exception {
//        new Thread(new Client()).start();
        final Server server = new ServerImpl();
        for (int i = 0; i < 100; i++) {
            final Registry registry = Simon.createRegistry(22222);
            registry.rebind("server", server);
            System.err.println("Server up and running!");

            Thread.sleep(10000);
            try {
                registry.unbind("server");
            } catch (Exception x) {
                System.err.println("unbind server " + x);
            }
            try {
                registry.stop();
            } catch (Exception x) {
                System.err.println("stop registry " + x);
            }

            System.out.println("Server stopped ...");
        }
        System.exit(0);
    }

    public void foo() throws SimonRemoteException {
        System.err.println("foo()");
    }
}
