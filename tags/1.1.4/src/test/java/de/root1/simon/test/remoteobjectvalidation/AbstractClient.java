package de.root1.simon.test.remoteobjectvalidation;

public abstract class AbstractClient implements IClient {

    @Override
    public void callback() {
        System.out.println("callback from server");
    }
}
