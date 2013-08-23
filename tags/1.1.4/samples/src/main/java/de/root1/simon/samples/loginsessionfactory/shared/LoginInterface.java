/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.samples.loginsessionfactory.shared;

public interface LoginInterface {

   public SessionInterface login(String user, String pass) throws LoginFailedException;

}
