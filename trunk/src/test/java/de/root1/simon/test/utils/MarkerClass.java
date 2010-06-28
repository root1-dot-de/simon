/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test.utils;

/**
 *
 * @author ACHR
 */
public class MarkerClass implements MarkerInterface {

    @Override
    public void helloPublic() {
        System.out.println("Hello public");
    }

    public void helloPrivate() {
        System.out.println("Hello private");
    }

}
