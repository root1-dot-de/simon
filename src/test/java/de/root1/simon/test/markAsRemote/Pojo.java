/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test.markAsRemote;

/**
 *
 * @author achristian
 */
public class Pojo implements IPojo {

    public void printHelloName(String name) {
        System.out.println(concat("Hello ",name));
    }

    public String getHelloName(String name) {
        return concat("Hello ",name);
    }

    private String concat(String a, String b) {
        return a+b;
    }

}
