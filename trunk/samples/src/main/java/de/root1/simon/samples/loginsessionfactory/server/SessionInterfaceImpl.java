/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.samples.loginsessionfactory.server;

import de.root1.simon.annotation.SimonRemote;
import de.root1.simon.samples.loginsessionfactory.shared.SessionInterface;

/**
 *
 * @author ACHR
 */
@SimonRemote(value={SessionInterface.class})
public class SessionInterfaceImpl implements SessionInterface {

    public void sessionMethodA() {
        System.out.println("invoked sessionMethodA()");
    }

    public void sessionMethodB() {
        System.out.println("invoked sessionMethodB()");
    }

    public void sessionMethodC() {
        System.out.println("invoked sessionMethodC()");
    }

}
