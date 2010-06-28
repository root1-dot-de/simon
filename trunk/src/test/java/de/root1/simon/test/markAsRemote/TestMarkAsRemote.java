/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test.markAsRemote;

import de.root1.simon.Lookup;
import de.root1.simon.Registry;
import de.root1.simon.Simon;
import org.junit.Test;

/**
 *
 * @author ACHR
 */
public class TestMarkAsRemote {

    @Test
    public void testMarkAndBindAndUse() {

        try {
            Pojo p = new Pojo();
            IPojo markedAsRemote = (IPojo) Simon.markAsRemote(p);
            Registry registry = Simon.createRegistry(22222);

            registry.bind("test", markedAsRemote);
            Lookup nameLookup = Simon.createNameLookup("localhost", 22222);
            IPojo remotePojo = (IPojo) nameLookup.lookup("test");

            System.out.println(remotePojo.getHelloName("Tester"));
            remotePojo.printHelloName("Testung");

            nameLookup.release(remotePojo);
            registry.unbind("test");
            registry.stop();
        } catch (Throwable t) {
            t.printStackTrace();
            new AssertionError(t.getMessage());
        }
    }

    @Test
    public void testMarkWithoutAnyInterface(){
        try {
            Simon.markAsRemote(this);
            throw new AssertionError("Objects without interface are not allowed/useable!");
        } catch (IllegalArgumentException ex){

        }
    }

}
