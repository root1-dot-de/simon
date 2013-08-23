/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.root1.simon.test.unreferenced;

public interface ServerInterface {

   public SessionInterface login(String user, ClientCallbackInterface clientCallback);

}