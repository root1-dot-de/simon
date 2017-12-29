/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.root1.simon.test.annotation;

import de.root1.simon.annotation.SimonRemote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author achristian
 */
public class ObjectWithNestedInterface {

    Logger logger = LoggerFactory.getLogger(getClass());

    public interface ServerAPI { public void stuff(); }
    
    @SimonRemote(ServerAPI.class)
    public class ServerAPIImpl implements ServerAPI {

        @Override
        public void stuff() {
            logger.info("stuff invoked on "+getClass().getName());
        }
    }
    
    public ServerAPI createInstance() {
        return new ServerAPIImpl();
    }

}
