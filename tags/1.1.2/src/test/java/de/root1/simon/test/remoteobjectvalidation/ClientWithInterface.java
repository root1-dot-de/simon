package de.root1.simon.test.remoteobjectvalidation;

import de.root1.simon.annotation.SimonRemote;

@SimonRemote(value = {IClient.class})
public class ClientWithInterface extends AbstractClient {
}
