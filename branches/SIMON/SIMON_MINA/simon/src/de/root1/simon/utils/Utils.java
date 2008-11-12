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
	

}
