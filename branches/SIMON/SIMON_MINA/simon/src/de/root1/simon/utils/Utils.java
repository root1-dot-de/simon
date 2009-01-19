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

/**
 * 
 * A class with some static helper-methods
 * 
 * @author ACHR
 */
public class Utils {
	
	private final static Logger logger = LoggerFactory.getLogger(Utils.class);
	
	/** if this flag is set to TRUE, SIMON tries to load the java.util.logging properties and enabled the debug-mode */
	public static boolean DEBUG = false;
		
	/** 
	 * A map that memories some method hashes so that they need not to be re-generated each time the hash is used.
	 * If memory is getting short, some entries are gc'ed so that more memory is available. There is no need to
	 * clear the map ourselves.
	 */
	private static WeakHashMap<Method, Long> methodHashs = new WeakHashMap<Method, Long>();


	/**
	 * Compute the "method hash" of a remote method. The method hash is a long
	 * containing the first 64 bits of the SHA digest from the bytes representing
	 * the complete method signature.
	 */
	public static long computeMethodHash(Method m) {
		
		if (methodHashs.containsKey(m)) {

			synchronized (methodHashs) {
				logger.trace("Got hash from map. map contains {} entries.", methodHashs.size());
				return methodHashs.get(m);
			}
			
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

			synchronized (methodHashs) {
				methodHashs.put(m, result);
				logger.trace("computed new hash. map now contains {} entries.", methodHashs.size());
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
			SimonProtocolCodecFactory instance = (SimonProtocolCodecFactory) clazz
					.newInstance();
			return instance;
		} catch (ClassCastException e) {
			throw new ClassCastException(
					"The given class '"
							+ protocolFactory
							+ "' must extend 'de.root1.simon.codec.base.SimonProtocolCodecFactory' !");
		}
	}
	
	/**
	 * Converts a long value to a hex string, i.e. 0xF923
	 * 
	 * @param l
	 * @return return a string showing the hex value of parameter l
	 */
	public static String longToHexString(long l){
		
		String id = Long.toHexString(l).toUpperCase();

        // Somewhat inefficient, but it won't happen that often
        // because an ID is often a big integer.
        while (id.length() < 8) {
            id = '0' + id; // padding
        }
        id = "0x" + id;

        return id;
	}
	
	/**
	 * Converts a boolean value to a byte value.
	 * @param bool
	 * @return 0xFF if true, 0x00 if false
	 */
	public static byte booleanToByte(boolean bool){
		return (bool==true ? (byte)0xFF : (byte)0x00 );
	}
	
	/**
	 * Converts a byte value to a boolean value.
	 * 
	 * @param b
	 * @return 0xFF if true, 0x00 if false
	 * @throws IllegalArgumentException if byte value not 0xFF or 0x00
	 */
	public static boolean byteToBoolean(byte b) throws IllegalArgumentException{
		switch (b) {
			case (byte) 0xFF:
				return true;
				
			case (byte) 0x00:
				return false;
	
			default:
				throw new IllegalArgumentException("only 0xFF and 0x00 value allowed for 'byte-to-boolean' conversion!");
		}
	}
	

}
