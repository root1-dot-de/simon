/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test.annotation;

import de.root1.simon.Lookup;
import de.root1.simon.Registry;
import de.root1.simon.Simon;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.LogManager;

/**
 *
 * @author achristian
 */
public class ManualTestAnnotation {

    public static void main(String[] args) {

//        File f = new File("target/test-classes/simon_logging.properties");
//        try {
//            FileInputStream is = new FileInputStream(f);
//            LogManager.getLogManager().readConfiguration(is);
//            System.out.println("logger initiated");
//
//        } catch (FileNotFoundException e) {
//
//                System.err.println("File not found: "+f.getAbsolutePath()+".\n" +
//                                "If you don't want to debug SIMON, leave 'Utils.DEBUG' with false-value.\n" +
//                                "Otherwise you have to provide a Java Logging API conform properties-file like mentioned.");
//
//        } catch (SecurityException e) {
//
//                System.err.println("Security exception occured while trying to load "+f.getAbsolutePath()+"\n" +
//                                "Logging with SIMON not possible!.");
//
//        } catch (IOException e) {
//
//                System.err.println("Cannot load "+f.getAbsolutePath()+" ...\n" +
//                                "Please make sure that Java has access to that file.");
//
//        }

        try {
            RemoteObjectImpl roi = new RemoteObjectImpl();

            Registry r = Simon.createRegistry(22222);
            r.bind("roi", roi);
            Lookup lookup = Simon.createLookup("localhost", 22222);

            RemoteObject roiRemote = (RemoteObject) lookup.lookup("roi");

            System.out.println("got remote object: "+roiRemote);

//            System.out.println("equals: "+roiRemote.equals(roiRemote));
//
//            System.out.println("equals called");
            roiRemote.myRemoteMethod();

            System.out.println("myremotemethod called");

            Simon.release(roiRemote);

            System.out.println("released ...");

            r.unbind("roi");
            r.stop();

            System.out.println("done");

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}
