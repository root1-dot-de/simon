/*
 * Copyright (C) 2008 Alexander Christian <alex(at)root1.de>. All rights reserved.
 * 
 * This file is part of SIMON.
 *
 *   SIMON is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SIMON is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with SIMON.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.root1.simon.utils;

import de.root1.simon.SimonRemoteMarker;
import de.root1.simon.annotation.SimonRemote;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.WeakHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.root1.simon.codec.base.SimonProtocolCodecFactory;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * 
 * A class with some static helper-methods
 * 
 * @author ACHR
 */
public class Utils {

    private final static Logger logger = LoggerFactory.getLogger(Utils.class);
    /**
     * if this flag is set to TRUE, SIMON tries to load the java.util.logging properties and enabled the debug-mode
     * @deprecated use JVM argument "java.util.logging.config.file=./log/mylogconfig.properties"
     */
    public static boolean DEBUG = false;
    /**
     * A map that memories some method hashes so that they need not to be re-generated each time the hash is used.
     * If memory is getting short, some entries are gc'ed so that more memory is available. There is no need to
     * clear the map ourselves.
     */
    private static final WeakHashMap<Method, Long> methodHashes = new WeakHashMap<Method, Long>();

    /**
     * Compute the "method hash" of a remote method. The method hash is a long
     * containing the first 64 bits of the SHA digest from the bytes representing
     * the complete method signature.
     * @param m the method for which the hash has to be computed
     * @return the computed hash
     */
    public static long computeMethodHash(Method m) {

        Long hash = null;
        synchronized (methodHashes) {
            hash = methodHashes.get(m);
        }
        if (hash != null) {

            logger.trace("Got hash from map. map contains {} entries.", methodHashes.size());
            return hash.longValue();

        } else {

            long result = 0;
            ByteArrayOutputStream byteArray = new ByteArrayOutputStream(127);

            try {
                MessageDigest md = MessageDigest.getInstance("SHA");

                DigestOutputStream out = new DigestOutputStream(byteArray, md);

                // use the complete method signature to generate the sha-digest
                out.write(m.toGenericString().getBytes());

                // use only the first 64 bits of the digest for the hash
                out.flush();
                byte hasharray[] = md.digest();
                for (int i = 0; i < Math.min(8, hasharray.length); i++) {
                    result += ((long) (hasharray[i] & 0xFF)) << (i * 8);
                }
            } catch (IOException ignore) {
                // can't really happen
                result = -1;
            } catch (NoSuchAlgorithmException complain) {
                throw new SecurityException(complain.getMessage());
            }

            synchronized (methodHashes) {
                methodHashes.put(m, result);
                logger.trace("computed new hash. map now contains {} entries.", methodHashes.size());
            }

            return result;
        }
    }

    /**
     * Loads a protocol codec factory by a given classname
     *
     * @param protocolFactory
     *            a class name like
     *            "com.mydomain.myproject.codec.mySimonProtocolCodecFactory"
     *            which points to a class, that extends
     *            {@link SimonProtocolCodecFactory}. <i>The important thing is,
     *            that this class correctly overrides
     *            {@link SimonProtocolCodecFactory#setup(boolean)}. For further
     *            details, look at {@link SimonProtocolCodecFactory}!</i>
     * @return the protocolcodecfactory instance according to the given protocol factory class name
     * @throws IllegalAccessException
     *             if the class or its nullary constructor is not accessible.
     * @throws InstantiationException
     *             if this Class represents an abstract class, an interface, an
     *             array class, a primitive type, or void; or if the class has
     *             no nullary constructor; or if the instantiation fails for
     *             some other reason.
     * @throws ClassNotFoundException
     *             if the class is not found by the classloader. if so, please
     *             check your classpath.
     * @throws ClassCastException
     *             if the given class is no instance of
     *             {@link SimonProtocolCodecFactory}
     */
    public static SimonProtocolCodecFactory getProtocolFactoryInstance(String protocolFactory)
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException {
        Class<?> clazz = Class.forName(protocolFactory);
        try {
            SimonProtocolCodecFactory instance = (SimonProtocolCodecFactory) clazz.newInstance();
            return instance;
        } catch (ClassCastException e) {
            throw new ClassCastException(
                    "The given class '"
                    + protocolFactory
                    + "' must extend '" + de.root1.simon.codec.base.SimonProtocolCodecFactory.class.getCanonicalName() + "' !");
        }
    }

    /**
     * Converts a long value to a hex string, i.e. 0xF923
     *
     * @param l
     * @return return a string showing the hex value of parameter l
     */
    public static String longToHexString(long l) {

        StringBuilder id = new StringBuilder();
        id.append(Long.toHexString(l).toUpperCase());

        while (id.length() < 8) {
            id.insert(0, "0");
        }
        id.insert(0, "0x");

        return id.toString();
    }

    /**
     * Converts a boolean value to a byte value.
     * @param bool
     * @return 0xFF if true, 0x00 if false
     */
    public static byte booleanToByte(boolean bool) {
        return (bool == true ? (byte) 0xFF : (byte) 0x00);
    }

    /**
     * Converts a byte value to a boolean value.
     *
     * @param b
     * @return 0xFF if true, 0x00 if false
     * @throws IllegalArgumentException if byte value not 0xFF or 0x00
     */
    public static boolean byteToBoolean(byte b) throws IllegalArgumentException {
        switch (b) {
            case (byte) 0xFF:
                return true;

            case (byte) 0x00:
                return false;

            default:
                throw new IllegalArgumentException("only 0xFF and 0x00 value allowed for 'byte-to-boolean' conversion!");
        }
    }

    /**
     * Method that returns an Class<?> array containing all remote interfaces of a given class
     * @param clazz the class to analyse for remote interfaces
     * @return the array with all known remote interfaces
     */
    public static Class<?>[] findAllRemoteInterfaces(Class<?> clazz) {
        Set<Class<?>> interfaceSet = doFindAllRemoteInterfaces(clazz);

        Class<?>[] interfaces = new Class[interfaceSet.size()];
        return interfaceSet.toArray(interfaces);
    }

    /**
     * Internal helper method for finding remote interfaces
     * @param clazz the class to analyse for remote interfaces
     * @return a set with remote interfaces
     */
    private static Set<Class<?>> doFindAllRemoteInterfaces(Class<?> clazz) {
        Set<Class<?>> interfaceSet = new HashSet<Class<?>>();

        // check for allowed remote interfaces
        Class[] remoteInterfaces = null;

        SimonRemote annotation = clazz.getAnnotation(SimonRemote.class);
        if (annotation != null) {
            remoteInterfaces = annotation.value();
        }

        if (remoteInterfaces == null || remoteInterfaces.length == 0) {

            if (logger.isTraceEnabled()) {
                if (annotation == null) {
                    logger.trace("No SimonRemote annotation found for clazz {}. Adding all known interfaces: {}", clazz.getName(), Arrays.toString(clazz.getInterfaces()));
                } else {
                    logger.trace("SimonRemote annotation found for clazz {}, but no remote interfaces specified. Adding all known interfaces: {}", clazz.getName(), Arrays.toString(clazz.getInterfaces()));
                }
            }
            //System.out.println("clazz "+ clazz.getName()+". Adding all known interfaces: "+ Arrays.toString(clazz.getInterfaces()));
            for (Class<?> interfaze : clazz.getInterfaces()) {
                //            if (SimonRemote.class.isAssignableFrom(interfaze)) {
                interfaceSet.add((Class<?>) interfaze);
                //            }
            }

        } else {

            if (logger.isTraceEnabled()) {
                logger.trace("SimonRemote annotation found. Remote interfaces where explicitly specified: {}. Adding them...", Arrays.toString(remoteInterfaces));
            }
            for (Class<?> interfaze : remoteInterfaces) {
                interfaceSet.add((Class<?>) interfaze);
            }

        }

        if (clazz.getSuperclass() != null
                && !clazz.getSuperclass().equals(Object.class)) {
            interfaceSet.addAll(doFindAllRemoteInterfaces(clazz.getSuperclass()));
        }

        return interfaceSet;
    }

    /**
     * Checks whether the object is annotated with <code>SimonRemote</code> or not
     * @param remoteObject the object to check
     * @return true, if object is annotated, false if not
     */
    public static boolean isRemoteAnnotated(Object remoteObject) {
        if (remoteObject == null) {
            throw new IllegalArgumentException("Cannot check a null-argument. You have to provide a proxy object instance ...");
        }
        return remoteObject.getClass().isAnnotationPresent(de.root1.simon.annotation.SimonRemote.class);
    }

    /**
     * Checks if the given remote object is a valid remote object
     * @param remoteObject the object to check
     * @return true, if remote object is valid, false if not
     */
    public static boolean isValidRemote(Object remoteObject) {
        if (remoteObject == null) {
            return false;
        }
        if (isRemoteAnnotated(remoteObject)) {
            return true;
        }
        if (getMarker(remoteObject) != null) {
            return true;
        }
        if (remoteObject instanceof de.root1.simon.SimonRemote) {
            return true;
        }
        return false;
    }

    /**
     * Returns the related instance of {@link SimonRemoteMarker} of the given object.
     * if the specified object isn't marked, null is returned.
     * @param o
     * @return the related instance of {@link SimonRemoteMarker}, or null if given object is not marked
     */
    public static SimonRemoteMarker getMarker(Object o) {
        if (o instanceof Proxy) {
            InvocationHandler invocationHandler = Proxy.getInvocationHandler(o);
            if (invocationHandler instanceof SimonRemoteMarker) {
                return (SimonRemoteMarker) invocationHandler;
            }
        }
        return null;
    }

    /**
     * Small helper method that pushes all interfaces of the specified class to the specified stack
     * @param stack
     * @param clazz
     */
    private static void putInterfacesToStack(Stack<Class> stack, Class clazz) {
        Class[] interfaces = clazz.getInterfaces();
        for (Class iClazz : interfaces) {
            stack.push(iClazz);
        }
    }

    /**
     * Reads all interfaces and subinterfaces of the given object and add the names to the provided interface name list
     * @param object the object to search for interfaces
     * @param interfaceNames the list to which found interfaces names are added
     */
    public static void putAllInterfaceNames(Object object, List<String> interfaceNames) {
        Stack<Class> stack = new Stack<Class>();
        Utils.putInterfacesToStack(stack, object.getClass());
        while (!stack.empty()) {
            Class iClazz = stack.pop();
            String iClazzName = iClazz.getCanonicalName();
            logger.trace("Adding {} to the list of remote interfaces", iClazzName);
            if (!interfaceNames.contains(iClazzName)) {
                interfaceNames.add(iClazzName);
            }
            Utils.putInterfacesToStack(stack, iClazz);
        }
    }

    public static String getStackTraceAsString(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

}
