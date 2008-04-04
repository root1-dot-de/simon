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
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import de.root1.simon.Statics;
import de.root1.simon.TxPacket;

public class Utils {
	
	
	private static DecimalFormat df2 = new DecimalFormat("00");
	private static DecimalFormat df3 = new DecimalFormat("000");

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
    	Utils.debug("Endpoint.wrapValue() -> start");
//    	Utils.debug("Endpoint.wrapValue() -> size at start: "+bb.capacity()+" position at start: "+bb.position());
    	Utils.debug("Endpoint.wrapValue() -> value="+value);
    	
    	try {
    	
	    	if (type == void.class || value instanceof Throwable) {
	           	
	    		Utils.debug("Endpoint.wrapValue() -> void, writing as object, may be 'null' or a 'Throwable'");
	           	bb.put(objectToBytes(value));
	           	
	    	} else if (type == String.class) {
	    		
	    		if (Statics.DEBUG_MODE)	System.out.println("Endpoint.wrapValue() -> String");
	           	bb.put(stringToBytes(((String) value).toString()));
	           	
	    	} else 	if (type.isPrimitive()) {
	    		
	        	if (type == boolean.class) {
	        		
	        		Utils.debug("Endpoint.wrapValue() -> boolean");
	            	bb.put((byte) (((Boolean) value).booleanValue() ? 1 : 0));
	            	
	            } else if (type == byte.class) {
	            	
	            	Utils.debug("Endpoint.wrapValue() -> byte");
	            	bb.put(((Byte) value).byteValue());
	            	
	            } else if (type == char.class) {
	            	
	            	Utils.debug("Endpoint.wrapValue() -> char");
	            	bb.putChar(((Character) value).charValue());
	            	
	            } else if (type == short.class) {
	
	            	Utils.debug("Endpoint.wrapValue() -> short");
	            	bb.putShort(((Short) value).shortValue());
	            	
	            } else if (type == int.class) {
	            	
	            	Utils.debug("Endpoint.wrapValue() -> int");
	            	bb.putInt(((Integer) value).intValue());
	                
	            } else if (type == long.class) {
	            	
	            	Utils.debug("Endpoint.wrapValue() -> long");
	            	bb.putLong(((Long) value).longValue());
	            	
	            } else if (type == float.class) {
	            	
	            	Utils.debug("Endpoint.wrapValue() -> float");
	            	bb.putFloat(((Float) value).floatValue());
	            	
	            } else if (type == double.class) {
	            	
	            	Utils.debug("Endpoint.wrapValue() -> double");
	            	bb.putDouble(((Double) value).doubleValue());
	            	
	            } else {
	            	Utils.debug("Endpoint.wrapValue() -> unknown");
	                throw new IOException("Unknown primitive: " + type);
	            }
	        	
	        } else {
	        	
	        	Utils.debug("Endpoint.wrapValue() -> non primitive object");
//	        	bb = 
	        		bb.put(objectToBytes(value));
	        	
	        }
    	} catch (BufferOverflowException e){
    		// this is already handle by TxPacket
//    		Utils.debug("Utils.wrapValue() Buffer too small. Doubling!");
//    		bb = wrapValue(type, value, doubleByteBuffer(bb));
    	}
//    	Utils.debug("Endpoint.wrapValue() -> size at end: "+bb.capacity()+" position at end: "+bb.position());
    	Utils.debug("Endpoint.wrapValue() -> end");
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
	    Utils.debug("Utils.stringToBytes() size=4+"+(bb.length-4));
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
    	Utils.debug("Endpoint.unwrapValue() -> start");
    	
    	
    	if (type == void.class ) {
    		
    		Utils.debug("Endpoint.unwrapValue() -> void, reading an object, may be 'null' or a 'Throwable'");
    		return getObject(bb);
    		
    	} else if (type == String.class) {
    		
    		if (Statics.DEBUG_MODE)	System.out.println("Endpoint.unwrapValue() -> String -> end");
           	return getString(bb);
           	
    	} else 	if (type.isPrimitive()) {
    		
        	if (type == boolean.class) {
        
        		Utils.debug("Endpoint.unwrapValue() -> boolean -> end");
        		return Boolean.valueOf(bb.get()==1 ? true : false);
                
            } else if (type == byte.class) {
            	
            	Utils.debug("Endpoint.unwrapValue() -> byte -> end");
            	return Byte.valueOf(bb.get());
                
            } else if (type == char.class) {
            	
            	Utils.debug("Endpoint.unwrapValue() -> char -> end");
            	return Character.valueOf(bb.getChar());
                
            } else if (type == short.class) {

            	Utils.debug("Endpoint.unwrapValue() -> short -> end");
            	return Short.valueOf(bb.getShort());
                
            } else if (type == int.class) {
            	
            	Utils.debug("Endpoint.unwrapValue() -> int -> end");
            	return Integer.valueOf(bb.getInt());
                
            } else if (type == long.class) {
            	
            	Utils.debug("Endpoint.unwrapValue() -> long -> end");
            	return Long.valueOf(bb.getLong());
                
            } else if (type == float.class) {

            	Utils.debug("Endpoint.unwrapValue() -> float -> end");
            	return Float.valueOf(bb.getFloat());
                
            } else if (type == double.class) {
            	
            	Utils.debug("Endpoint.unwrapValue() -> double -> end");
            	return Double.valueOf(bb.getDouble());
                
            } else {
            	Utils.debug("Endpoint.unwrapValue() -> unknown -> end");
                throw new IOException("Unknown primitive: " + type);
            }
        	
        } else {
        	
        	Utils.debug("Endpoint.unwrapValue() -> non primitive object -> end");
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
		oos.flush();
//		oos.close();
		bb = new byte[baos.size()+4];
		
		int v = baos.size();
		bb[0] = (byte)((v >>> 24) & 0xFF);
		bb[1] = (byte)((v >>> 16) & 0xFF);
	    bb[2] = (byte)((v >>>  8) & 0xFF);
	    bb[3] = (byte)((v >>>  0) & 0xFF);
	    
	    System.arraycopy(baos.toByteArray(), 0, bb, 4, v);
//		bb = baos.toByteArray();
	    Utils.debug("Utils.objectToBytes() object="+object+" byte[].length="+bb.length);
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
    	
//    	if (bb.hasArray())
//    		b = bb.array();
//    	else
//    		throw new IllegalArgumentException("given ByteBuffer has no byte[] access");
    	
    	int length = bb.getInt();
    	byte[] objectInBytes = new byte[length];
    	bb.get(objectInBytes);
    	
//    	ByteArrayInputStream bais = new ByteArrayInputStream(b);
    	ByteArrayInputStream bais = new ByteArrayInputStream(objectInBytes);
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
    	Utils.debug("Utils.getString() -> length="+length);
    	byte[] stringInBytes = new byte[length];
    	bb.get(stringInBytes);
    	Utils.debug("Utils.getString() -> string="+new String(stringInBytes));
    	return new String(stringInBytes);
    }
    
	public synchronized static void debug(String msg) {
		if (Statics.DEBUG_MODE) {
			Calendar cal = GregorianCalendar.getInstance();
			StringBuffer sb = new StringBuffer();
			sb.append(df2.format(cal.get(Calendar.HOUR_OF_DAY)));
			sb.append(":");
			sb.append(df2.format(cal.get(Calendar.MINUTE)));
			sb.append(".");
			sb.append(df3.format(cal.get(Calendar.MILLISECOND)));
			sb.append(" - ");
			sb.append(msg);
			System.out.println(sb.toString());
			System.out.flush();
		}
	}
	
	
   	public static String printSelectionKeyValue(int key) {

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
	
}
