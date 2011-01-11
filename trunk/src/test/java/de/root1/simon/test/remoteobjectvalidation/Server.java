package de.root1.simon.test.remoteobjectvalidation;

import de.root1.simon.annotation.SimonRemote;

@SimonRemote
public class Server implements IServer {

    @Override
    public void test(IClient client) {
        System.out.println("Hello From Server");
        client.callback();
    }
}
