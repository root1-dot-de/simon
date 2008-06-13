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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.root1.simon.TxPacket;

public class Utils {
	
	protected static transient Logger _log = Logger.getLogger(Utils.class.getName());
	
	public static boolean DEBUG = false;


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
     * wrap the value into the given bytebuffer
     */
    public static TxPacket wrapValue(Class<?> type, Object value, TxPacket bb) throws IOException {
    	//Utils.debug("Utils.wrapValue() -> start");
//    	//Utils.debug("Endpoint.wrapValue() -> size at start: "+bb.capacity()+" position at start: "+bb.position());
    	//Utils.debug("Utils.wrapValue() -> value="+value);
    	
    	
    	if (type == void.class || value instanceof Throwable) {
           	
    		//Utils.debug("Utils.wrapValue() -> void, writing as object, may be 'null' or a 'Throwable'");
           	bb.put(objectToBytes(value));
           	
    	} else if (type == String.class) {
    		
    		//Utils.debug("Utils.wrapValue() -> String");
           	bb.put(stringToBytes(((String) value).toString()));
           	
    	} else 	if (type.isPrimitive()) {
    		
        	if (type == boolean.class) {
        		
        		//Utils.debug("Utils.wrapValue() -> boolean");
            	bb.put((byte) (((Boolean) value).booleanValue() ? 1 : 0));
            	
            } else if (type == byte.class) {
            	
            	//Utils.debug("Utils.wrapValue() -> byte");
            	bb.put(((Byte) value).byteValue());
            	
            } else if (type == char.class) {
            	
            	//Utils.debug("Utils.wrapValue() -> char");
            	bb.putChar(((Character) value).charValue());
            	
            } else if (type == short.class) {

            	//Utils.debug("Utils.wrapValue() -> short");
            	bb.putShort(((Short) value).shortValue());
            	
            } else if (type == int.class) {
            	
            	//Utils.debug("Utils.wrapValue() -> int");
            	bb.putInt(((Integer) value).intValue());
                
            } else if (type == long.class) {
            	
            	//Utils.debug("Utils.wrapValue() -> long");
            	bb.putLong(((Long) value).longValue());
            	
            } else if (type == float.class) {
            	
            	//Utils.debug("Utils.wrapValue() -> float");
            	bb.putFloat(((Float) value).floatValue());
            	
            } else if (type == double.class) {
            	
            	//Utils.debug("Utils.wrapValue() -> double");
            	bb.putDouble(((Double) value).doubleValue());
            	
            } else {
            	//Utils.debug("Utils.wrapValue() -> unknown");
                throw new IOException("Unknown primitive: " + type);
            }
        	
        } else {
        	
        	//Utils.debug("Utils.wrapValue() -> non primitive object");
    		bb.put(objectToBytes(value));
        	
        }
    	return bb;
    }

	/**
	 * Doubles the capacity of an ByteBuffer
	 * Already stored bytes in the buffer will be transfered to the new buffer
	 * @param bb the ByteBuffer to double
	 * @return the doubled ByteBuffer
	 */
	public static ByteBuffer doubleByteBuffer(ByteBuffer bb) {
		ByteBuffer bb_new = ByteBuffer.allocate(bb.capacity()*2);
		bb.flip();
		bb_new.put(bb);
		return bb_new;
	}

    public static byte[] stringToBytes(String value) {
    	byte[] bb;
		bb = new byte[value.length()+4];
		
		int v = bb.length-4;

	    System.arraycopy(integerToBytes(v), 0, bb, 0, 4);
	    System.arraycopy(value.getBytes(), 0, bb, 4, v);
	    //Utils.debug("Utils.stringToBytes() size=4+"+(bb.length-4));
		return bb;
	}

	public static Object integerToBytes(int v) {
		byte[] bb = new byte[4];
		bb[0] = (byte)((v >>> 24) & 0xFF);
		bb[1] = (byte)((v >>> 16) & 0xFF);
	    bb[2] = (byte)((v >>>  8) & 0xFF);
	    bb[3] = (byte)((v >>>  0) & 0xFF);
		return bb;
	}

	/**
     * unwrap the value from the given bytebuffer
     */
    public static Object unwrapValue(Class<?> type, ByteBuffer bb) throws IOException, ClassNotFoundException {
    	//Utils.debug("Utils.unwrapValue() -> start");
    	
    	
    	if (type == void.class ) {
    		
    		//Utils.debug("Utils.unwrapValue() -> void, reading an object, may be 'null' or a 'Throwable'");
    		return getObject(bb);
    		
    	} else if (type == String.class) {
    		
    		//Utils.debug("Utils.unwrapValue() -> String -> end");
           	return getString(bb);
           	
    	} else 	if (type.isPrimitive()) {
    		
        	if (type == boolean.class) {
        
        		//Utils.debug("Utils.unwrapValue() -> boolean -> end");
        		return Boolean.valueOf(bb.get()==1 ? true : false);
                
            } else if (type == byte.class) {
            	
            	//Utils.debug("Utils.unwrapValue() -> byte -> end");
            	return Byte.valueOf(bb.get());
                
            } else if (type == char.class) {
            	
            	//Utils.debug("Utils.unwrapValue() -> char -> end");
            	return Character.valueOf(bb.getChar());
                
            } else if (type == short.class) {

            	//Utils.debug("Utils.unwrapValue() -> short -> end");
            	return Short.valueOf(bb.getShort());
                
            } else if (type == int.class) {
            	
            	//Utils.debug("Utils.unwrapValue() -> int -> end");
            	return Integer.valueOf(bb.getInt());
                
            } else if (type == long.class) {
            	
            	//Utils.debug("Utils.unwrapValue() -> long -> end");
            	return Long.valueOf(bb.getLong());
                
            } else if (type == float.class) {

            	//Utils.debug("Utils.unwrapValue() -> float -> end");
            	return Float.valueOf(bb.getFloat());
                
            } else if (type == double.class) {
            	
            	//Utils.debug("Utils.unwrapValue() -> double -> end");
            	return Double.valueOf(bb.getDouble());
                
            } else {
            	//Utils.debug("Utils.unwrapValue() -> unknown -> end");
                throw new IOException("Unknown primitive: " + type);
            }
        	
        } else {
        	
        	//Utils.debug("Utils.unwrapValue() -> non primitive object -> end");
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
    	
//    	ByteBufferOutputStream baos = new ByteBufferOutputStream();
//		ObjectOutputStream oos = new ObjectOutputStream(baos);
	
		oos.writeObject(object);
		oos.flush();
		bb = new byte[baos.size()+4];
		
		int v = baos.size();
		bb[0] = (byte)((v >>> 24) & 0xFF);
		bb[1] = (byte)((v >>> 16) & 0xFF);
	    bb[2] = (byte)((v >>>  8) & 0xFF);
	    bb[3] = (byte)((v >>>  0) & 0xFF);
	    
	    System.arraycopy(baos.toByteArray(), 0, bb, 4, v);
	    //Utils.debug("Utils.objectToBytes() object="+object+" byte[].length="+bb.length);
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
    	
    	// TODO for optimization, see: http://www.theserverside.com/discussions/thread.tss?thread_id=21568
    	
    	bb.getInt();
//    	byte[] objectInBytes = new byte[bb.getInt()]; // read object size and create a byte[] for it
//    	bb.get(objectInBytes); // put the object to the byte[]
    	
    	// use the byte[] for deserializing the inherited object
//    	ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(objectInBytes));
    	ObjectInputStream ois = new ObjectInputStream(new ByteBufferInputStream(bb));
    	
    	// read and return the object from the stream
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
    	if (_log.isLoggable(Level.FINEST)){
    		_log.finest("length="+length);
    	}
    	byte[] stringInBytes = new byte[length];
    	bb.get(stringInBytes);
    	if (_log.isLoggable(Level.FINEST)){
    		_log.finest("string="+new String(stringInBytes));
    	}
    	return new String(stringInBytes);
    }
    
    /**
     * 
     * TODO: Documentation to be done for method 'getSelectionKeyString', by 'ACHR'..
     * 
     * @param key
     * @return
     */
    public static String getSelectionKeyString(int key) {

		StringBuilder sb = new StringBuilder();

		if ((key & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {
			sb.append("OP_ACCEPT, ");
		}

		if ((key & SelectionKey.OP_CONNECT) == SelectionKey.OP_CONNECT) {
			sb.append("OP_CONNECT, ");
		}

		if ((key & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE) {
			sb.append("OP_WRITE, ");
		}

		if ((key & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
			sb.append("OP_READ, ");
		}

		String txt = sb.toString();
		txt = txt.trim();

		if (txt.length() > 0) {
			txt = txt.substring(0, txt.length() - 1);
		}

		return txt + " (" + key + ")";
	}
    
	/**
	 * Gets a String represenatation for a <code>SocketChannel</code>
	 * 
	 * @param channel
	 * @return
	 */
	public static String getChannelString(SocketChannel channel) {
		return "["+channel+"]";
	}
	
	/**
	 * Gets a String representation for a <Code>SelectionKey>/code>
	 * TODO: Documentation to be done for method 'getKeyString', by 'ACHR'..
	 * 
	 * @param key
	 * @return
	 */
	public static String getKeyString(SelectionKey key){
		String ret = "[["+key.channel()+"]interestOps="+Utils.getSelectionKeyString(key.interestOps())+",readyOps="+Utils.getSelectionKeyString(key.readyOps())+"]"; 
		return ret;
	}
}
