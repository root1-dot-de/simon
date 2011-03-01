/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.samples.rawchannel2.shared;


/**
 *
 * @author ACHR
 */
public interface MyServer{

    public void doSomething();

     public void requestFile(ClientCallback clientCallback, String filename);

}
