/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test.exportobject;

import de.root1.simon.annotation.SimonRemote;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author achristian
 */
@SimonRemote
public class ExportObjectProxy implements InvocationHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final IPojo proxyObject;


    ExportObjectProxy(IPojo p) {
        this.proxyObject = p;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        logger.info("proxy={} method={} args={}", new Object[]{proxy, method, args});

        return method.invoke(proxy, args);
    }

    public Object getProxyObject() {
        return proxyObject;
    }

}
