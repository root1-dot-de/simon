/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.samples.loginsessionfactory.server;

import de.root1.simon.annotation.SimonRemote;
import de.root1.simon.samples.loginsessionfactory.shared.LoginFailedException;
import de.root1.simon.samples.loginsessionfactory.shared.LoginInterface;
import de.root1.simon.samples.loginsessionfactory.shared.SessionInterface;

@SimonRemote(value={LoginInterface.class})
public class LoginInterfaceImpl implements LoginInterface {

   private static final long serialVersionUID = 1L;

   public SessionInterface login(String user, String pass) throws LoginFailedException {
        if (user.equals("myAuthorizedUser") && pass.equals("myAuthorizedPass")) {
            return new SessionInterfaceImpl();
        }
        throw new LoginFailedException("Login for user " + user + " failed. Invalid password?");
   }
}
