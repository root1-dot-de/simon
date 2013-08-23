/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.samples.loginsessionfactory.server;

import de.root1.simon.exceptions.SimonRemoteException;
import de.root1.simon.samples.loginsessionfactory.shared.SessionInterface;

/**
 *
 * @author ACHR
 */
public class SessionInterfaceImpl implements SessionInterface {

    public void sessionMethodA() throws SimonRemoteException {
        System.out.println("invoked sessionMethodA()");
    }

    public void sessionMethodB() throws SimonRemoteException {
        System.out.println("invoked sessionMethodB()");
    }

    public void sessionMethodC() throws SimonRemoteException {
        System.out.println("invoked sessionMethodC()");
    }

}
