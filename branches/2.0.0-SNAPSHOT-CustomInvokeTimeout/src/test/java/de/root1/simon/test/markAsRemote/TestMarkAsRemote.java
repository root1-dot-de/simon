/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test.markAsRemote;

import de.root1.simon.Lookup;
import de.root1.simon.NameLookup;
import de.root1.simon.Registry;
import de.root1.simon.Simon;
import de.root1.simon.exceptions.IllegalRemoteObjectException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ACHR
 */
public class TestMarkAsRemote {
    
    private final Logger logger = LoggerFactory.getLogger(TestMarkAsRemote.class);

    @Test
    public void testMarkAndBindAndUse() {

        try {
            Pojo p = new Pojo();
            IPojo markedAsRemote = (IPojo) Simon.markAsRemote(p);
            Registry registry = Simon.createRegistry(22224);

            registry.bind("test", markedAsRemote);
            Lookup lookup = Simon.createNameLookup("localhost", 22224);
            IPojo remotePojo = (IPojo) lookup.lookup("test");

            System.out.println(remotePojo.getHelloName("Tester"));
            remotePojo.printHelloName("Testung");

            lookup.release(remotePojo);
            logger.info("Awaiting network connections shutdown");
            ((NameLookup)lookup).awaitCompleteShutdown(30000);
            logger.info("Awaiting network connections shutdown *done*");
            
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
        } catch (IllegalRemoteObjectException ex){

        }
    }

}
