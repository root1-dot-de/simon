/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test;

/**
 *
 * @author achristian
 */
@de.root1.simon.annotation.SimonRemote
public class RemoteObjectImpl implements RemoteObject {

    @Override
    public void helloWorld(){
//        System.out.println("Hello World");
    }

    @Override
    public void helloWorldArg(String s){
//        System.out.println("Hello WorldArg ["+s+"]");
    }

}
