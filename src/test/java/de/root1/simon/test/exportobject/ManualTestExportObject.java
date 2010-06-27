/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test.exportobject;

import de.root1.simon.Lookup;
import de.root1.simon.Registry;
import de.root1.simon.Simon;
import de.root1.simon.exceptions.EstablishConnectionFailed;
import de.root1.simon.exceptions.LookupFailedException;
import de.root1.simon.exceptions.NameBindingException;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 *
 * @author achristian
 */
public class ManualTestExportObject {

    public static void main(String[] args) throws UnknownHostException, IOException, NameBindingException, LookupFailedException, EstablishConnectionFailed {

//        Pojo p = new Pojo();
//        Object exportedObject = Simon.exportObject(p);
//        Registry registry = Simon.createRegistry(22222);
//
//        registry.bind("test", exportedObject);
//        Lookup nameLookup = Simon.createNameLookup("localhost", 22222);
//        Pojo remotePojo = (Pojo) nameLookup.lookup("test");
//
//        System.out.println(remotePojo.getHelloName("Tester"));
//        remotePojo.printHelloName("Testung");
//
//        nameLookup.release(remotePojo);
//        registry.unbind("test");
//        registry.stop();

        IPojo p = new Pojo();

        ExportObjectProxy ih = new ExportObjectProxy(p);

        Class<?>[] interfaces = p.getClass().getInterfaces();
        Object proxyObject = (Proxy) Proxy.newProxyInstance(ManualTestExportObject.class.getClassLoader(), interfaces, ih);

        System.out.println("proxy="+proxyObject.getClass());
        System.out.println("annotations="+Arrays.toString(proxyObject.getClass().getAnnotations()));

        if (proxyObject instanceof Proxy){
            InvocationHandler invocationHandler = Proxy.getInvocationHandler(proxyObject);
            if (invocationHandler instanceof ExportObjectProxy) {
                ExportObjectProxy epo = (ExportObjectProxy) invocationHandler;
                Class<?>[] interfaces1 = epo.getProxyObject().getClass().getInterfaces();

                

            }
        }
    }

}
