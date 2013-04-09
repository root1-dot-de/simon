package de.root1.simon.samples.rawchannelstream.server;

import de.root1.simon.Registry;
import de.root1.simon.Simon;
import de.root1.simon.exceptions.NameBindingException;
import java.io.IOException;
import java.net.UnknownHostException;

public class Server {

    public static void main(String[] args) {
        try {
            System.out.println("Server started ...");
            MyServerImpl myServerImpl = new MyServerImpl();
            Registry registry = Simon.createRegistry();
            registry.start();
            System.out.println("Registry created");
            registry.bind("myServer", myServerImpl);
            System.out.println("Remote Object bound ...");


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
