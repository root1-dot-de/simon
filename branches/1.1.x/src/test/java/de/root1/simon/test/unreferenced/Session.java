/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.root1.simon.test.unreferenced;

import de.root1.simon.SimonUnreferenced;
import de.root1.simon.annotation.SimonRemote;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SimonRemote(value = {SessionInterface.class})
public class Session implements SessionInterface,  SimonUnreferenced, Serializable {

    private final String user;
    private final ServerInterfaceImpl server;

    public Session(String user, ServerInterfaceImpl server) {
        this.user = user;
        this.server = server;
    }

    @Override
    public void doSomething() {
        System.out.println("User "+user+" does something ...");
    }

    @Override
    public void unreferenced() {
        System.out.println("Unreferenced: "+user+"@"+this);
        server.removeUserSession(this);
    }

    public String getUsername(){
        return user;
    }

}