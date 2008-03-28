package de.root1.simon.utils;

/*
 * Copyright 2008 Alexander Christian <alex(at)root1.de>. All rights reserved.
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import de.root1.simon.Statics;

public class Utils {

	/**
	 * Compute the "method hash" of a remote method. The method hash is a long
	 * containing the first 64 bits of the SHA digest from the bytes representing
	 * the complete method signature.
	 */
	public static long computeMethodHash(Method m) {
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
		return result;
	}
	
	/**
     * wrap the value with the according write method
     */
    public static ByteBuffer wrapValue(Class<?> type, Object value, ByteBuffer bb) throws IOException {
    	if (Statics.DEBUG_MODE) System.out.println("Endpoint.wrapValue() -> start");
    	if (Statics.DEBUG_MODE) System.out.println("Endpoint.wrapValue() -> size at start: "+bb.capacity()+" position at start: "+bb.position());
    	if (Statics.DEBUG_MODE) System.out.println("Endpoint.wrapValue() -> value="+value);
    	
    	try {
    	
	    	if (type == void.class || value instanceof Throwable) {
	           	if (Statics.DEBUG_MODE) System.out.println("Endpoint.wrapValue() -> void, writing as object, may be 'null' or a 'Throwable'");
	           	bb.put(objectToBytes(value));
	    	}
	    	else 
//	    	if (type == String.class) {
//	    		if (Statics.DEBUG_MODE)	System.out.println("Endpoint.wrapValue() -> String");
//	           	bb.put(stringToBytes(((String) value).toString()));
//	    	}
	    	if (type.isPrimitive()) {
	        	if (type == boolean.class) {
	        		
	        		if (Statics.DEBUG_MODE) System.out.println("Endpoint.wrapValue() -> boolean");
	            	bb.put((byte) (((Boolean) value).booleanValue() ? 1 : 0));
	            	
	            } else if (type == byte.class) {
	            	
	            	if (Statics.DEBUG_MODE) System.out.println("Endpoint.wrapValue() -> byte");
	            	bb.put(((Byte) value).byteValue());
	            	
	            } else if (type == char.class) {
	            	
	            	if (Statics.DEBUG_MODE) System.out.println("Endpoint.wrapValue() -> char");
	            	bb.putChar(((Character) value).charValue());
	            	
	            } else if (type == short.class) {
	
	            	if (Statics.DEBUG_MODE) System.out.println("Endpoint.wrapValue() -> short");
	            	bb.putShort(((Short) value).shortValue());
	            	
	            } else if (type == int.class) {
	            	
	            	if (Statics.DEBUG_MODE) System.out.println("Endpoint.wrapValue() -> int");
	            	bb.putInt(((Integer) value).intValue());
	                
	            } else if (type == long.class) {
	            	
	            	if (Statics.DEBUG_MODE) System.out.println("Endpoint.wrapValue() -> long");
	            	bb.putLong(((Long) value).longValue());
	            	
	            } else if (type == float.class) {
	            	
	            	if (Statics.DEBUG_MODE) System.out.println("Endpoint.wrapValue() -> float");
	            	bb.putFloat(((Float) value).floatValue());
	            	
	            } else if (type == double.class) {
	            	
	            	if (Statics.DEBUG_MODE) System.out.println("Endpoint.wrapValue() -> double");
	            	bb.putDouble(((Double) value).doubleValue());
	            	
	            } else {
	            	if (Statics.DEBUG_MODE) System.out.println("Endpoint.wrapValue() -> unknown");
	                throw new IOException("Unknown primitive: " + type);
	            }
	        } else {
	        	if (Statics.DEBUG_MODE) System.out.println("Endpoint.wrapValue() -> non primitive object");
	        	bb = bb.put(objectToBytes(value));
	        }
    	} catch (BufferOverflowException e){
    		if (Statics.DEBUG_MODE) System.out.println("Utils.wrapValue() Buffer too small. Doubling!");
    		ByteBuffer bb_new = ByteBuffer.allocate(bb.capacity()*2);
    		bb.flip();
    		bb_new.put(bb);
    		bb = wrapValue(type, value, bb_new);
    	}
    	if (Statics.DEBUG_MODE) System.out.println("Endpoint.wrapValue() -> size at end: "+bb.capacity()+" position at end: "+bb.position());
    	if (Statics.DEBUG_MODE) System.out.println("Endpoint.wrapValue() -> end");
    	return bb;
    }

    public static byte[] stringToBytes(String value) {
    	byte[] bb;
		bb = new byte[value.length()+4];
		
		int v = bb.length-4;
		bb[0] = (byte)((v >>> 24) & 0xFF);
		bb[1] = (byte)((v >>> 16) & 0xFF);
	    bb[2] = (byte)((v >>>  8) & 0xFF);
	    bb[3] = (byte)((v >>>  0) & 0xFF);
	    
	    System.arraycopy(value.getBytes(), 0, bb, 4, v);
	    if (Statics.DEBUG_MODE) System.out.println("Utils.stringToBytes() size="+bb.length);
		return bb;
	}

	/**
     * unwrap the value with the according read method
     */
    public static Object unwrapValue(Class<?> type, ByteBuffer bb) throws IOException, ClassNotFoundException {
    	if (Statics.DEBUG_MODE) System.out.println("Endpoint.unwrapValue() -> start");
    	
    	
    	if (type == void.class ) {
    		if (Statics.DEBUG_MODE) System.out.println("Endpoint.unwrapValue() -> void, reading an object, may be 'null' or a 'Throwable'");
    		return getObject(bb);
    	}
    	else
    	if (type.isPrimitive()) {
        	if (type == boolean.class) {
        
        		if (Statics.DEBUG_MODE) System.out.println("Endpoint.unwrapValue() -> boolean -> end");
        		return Boolean.valueOf(bb.get()==1 ? true : false);
                
            } else if (type == byte.class) {
            	
            	if (Statics.DEBUG_MODE) System.out.println("Endpoint.unwrapValue() -> byte -> end");
            	return Byte.valueOf(bb.get());
                
            } else if (type == char.class) {
            	
            	if (Statics.DEBUG_MODE) System.out.println("Endpoint.unwrapValue() -> char -> end");
            	return Character.valueOf(bb.getChar());
                
            } else if (type == short.class) {

            	if (Statics.DEBUG_MODE) System.out.println("Endpoint.unwrapValue() -> short -> end");
            	return Short.valueOf(bb.getShort());
                
            } else if (type == int.class) {
            	
            	if (Statics.DEBUG_MODE) System.out.println("Endpoint.unwrapValue() -> int -> end");
            	return Integer.valueOf(bb.getInt());
                
            } else if (type == long.class) {
            	
            	if (Statics.DEBUG_MODE) System.out.println("Endpoint.unwrapValue() -> long -> end");
            	return Long.valueOf(bb.getLong());
                
            } else if (type == float.class) {

            	if (Statics.DEBUG_MODE) System.out.println("Endpoint.unwrapValue() -> float -> end");
            	return Float.valueOf(bb.getFloat());
                
            } else if (type == double.class) {
            	
            	if (Statics.DEBUG_MODE) System.out.println("Endpoint.unwrapValue() -> double -> end");
            	return Double.valueOf(bb.getDouble());
                
            } else {
            	if (Statics.DEBUG_MODE) System.out.println("Endpoint.unwrapValue() -> unknown -> end");
                throw new IOException("Unknown primitive: " + type);
            }
        } else {
        	if (Statics.DEBUG_MODE) System.out.println("Endpoint.unwrapValue() -> non primitive object -> end");
        	return getObject(bb);
        }
    }
    
    /**
     * Converts an object to a byte[]
     * For "easy" reading, the first 4 bytes are an integer, 
     * indicating the length of following object
     * 
     * @param object the object to convert
     * @param bb the target byte[] where the length and the object is stored
     * @throws IOException if there's a problem with the serialisation of the object
     */
    public static byte[] objectToBytes(Object object) throws IOException{
    	byte[] bb;
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
	
		oos.writeObject(object);
		oos.close();
		bb = new byte[baos.size()+4];
		
		int v = baos.size();
		bb[0] = (byte)((v >>> 24) & 0xFF);
		bb[1] = (byte)((v >>> 16) & 0xFF);
	    bb[2] = (byte)((v >>>  8) & 0xFF);
	    bb[3] = (byte)((v >>>  0) & 0xFF);
	    
	    System.arraycopy(baos.toByteArray(), 0, bb, 4, v);
	    if (Statics.DEBUG_MODE) System.out.println("Utils.objectToBytes() size="+bb.length);
		return bb;
    }
    
    /**
     * 
     * Returns the Object stored in this ByteBuffer.
     * The bytebuffer has to have the correct size 
     * and must be completely filled with the serialized 
     * object. That means: No extra bytes "before" the 
     * object in the buffer, and no empty/unused bytes 
     * "after" the object in the buffer
     * 
     * @param bb the bytebuffer containing the serialized object
     * @return the object stored in the bytebuffer
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object getObject(ByteBuffer bb) throws IOException, ClassNotFoundException{
    	byte[] b = null;
    	
    	if (bb.hasArray())
    		b = bb.array();
    	else
    		throw new IllegalArgumentException("given ByteBuffer has now byte[] access");
    	
    	ByteArrayInputStream bais = new ByteArrayInputStream(b);
    	ObjectInputStream ois = new ObjectInputStream(bais);
    	
    	return ois.readObject();
    }
    
    /**
     * 
     * Returns the String stored in this ByteBuffer.
     * The bytebuffer has to have the correct size 
     * and must be completely filled with the String's 
     * bytes. That means: No extra bytes "before" the 
     * String in the buffer, and no empty/unused bytes 
     * "after" the String in the buffer
     * 
     * @param bb the bytebuffer containing the String's bytes
     * @return the String stored in the bytebuffer
     * @throws IOException
     * @throws ClassNotFoundException
     */  
    public static String getString(ByteBuffer bb){
    	int length = bb.getInt();
    	byte[] stringInBytes = new byte[length];
    	bb.get(stringInBytes);
    	return new String(stringInBytes);
    }

}
