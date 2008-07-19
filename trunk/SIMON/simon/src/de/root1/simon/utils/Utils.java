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
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.root1.simon.Statics;
import de.root1.simon.TxPacket;
import de.root1.simon.tests.DirectByteBufferPool;

/**
 * 
 * A class with some static helper-methods
 * 
 * @author ACHR
 */
public class Utils {
	
	protected static transient Logger _log = Logger.getLogger(Utils.class.getName());
	
	/** if this flag is set to TRUE, SIMON tries to load the java.util.logging properties and enabled the debug-mode */
	public static boolean DEBUG = false;
		
	/** 
	 * A map that memories some method hashs so that they need not to be re-generated each time the hash is used.
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
				if (_log.isLoggable(Level.FINEST))
					_log.finest("Got hash from map. map contains "+methodHashs.size()+" entries.");
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
				if (_log.isLoggable(Level.FINEST))
					_log.finest("computed new hash. map now contains "+methodHashs.size()+" entries.");
			}
			
			return result;
		}
	}
	
	/**
     * wrap the value into the given bytebuffer
     */
    public static TxPacket wrapValue(Class<?> type, Object value, TxPacket bb) throws IOException {
    	
    	_log.finest("start");
    	
    	if (type == void.class || value instanceof Throwable) {
           	
    		_log.finest("void, writing as object, may be 'null' or 'Throwable'");
           	bb.put(objectToBytes(value));
           	
    	} else if (type == String.class) {
    		
    		_log.finest("String");
           	bb.put(stringToBytes(((String) value).toString()));
           	
    	} else 	if (type.isPrimitive()) {
    		
        	if (type == boolean.class) {
        		
        		_log.finest("boolean");
            	bb.put((byte) (((Boolean) value).booleanValue() ? 1 : 0));
            	
            } else if (type == byte.class) {
            	
            	_log.finest("byte");
            	bb.put(((Byte) value).byteValue());
            	
            } else if (type == char.class) {
            	
            	_log.finest("char");
            	bb.putChar(((Character) value).charValue());
            	
            } else if (type == short.class) {

            	_log.finest("short");
            	bb.putShort(((Short) value).shortValue());
            	
            } else if (type == int.class) {
            	
            	_log.finest("int");
            	bb.putInt(((Integer) value).intValue());
                
            } else if (type == long.class) {
            	
            	_log.finest("long");
            	bb.putLong(((Long) value).longValue());
            	
            } else if (type == float.class) {
            	
            	_log.finest("float");
            	bb.putFloat(((Float) value).floatValue());
            	
            } else if (type == double.class) {
            	
            	_log.finest("double");
            	bb.putDouble(((Double) value).doubleValue());
            	
            } else {
            	_log.finest("unknown");
                throw new IOException("Unknown primitive: " + type);
            }
        	
        } else {
        	
        	_log.finest("non primitive object");
    		bb.put(objectToBytes(value));
        	
        }
    	_log.finest("end");
    	return bb;
    }

	/**
	 * Doubles the capacity of an ByteBuffer
	 * Already stored bytes in the buffer will be transfered to the new buffer
	 * @param bb the ByteBuffer to double
	 * @return the doubled ByteBuffer
	 */
	public static ByteBuffer doubleByteBuffer(ByteBuffer bb) {
//		ByteBuffer bb_new = ByteBuffer.allocate(bb.capacity()*2);
		ByteBuffer bb_new = DirectByteBufferPool.getInstance().getByteBuffer(bb.capacity()*2);
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
    	_log.finest("start");
    	
    	
    	if (type == void.class ) {
    		
    		//Utils.debug("Utils.unwrapValue() -> void, reading an object, may be 'null' or a 'Throwable'");
    		return getObject(bb);
    		
    	} else if (type == String.class) {
    		
    		_log.finest("String -> end");
           	return getString(bb);
           	
    	} else 	if (type.isPrimitive()) {
    		
        	if (type == boolean.class) {
        
        		_log.finest("boolean -> end");
        		return Boolean.valueOf(bb.get()==1 ? true : false);
                
            } else if (type == byte.class) {
            	
            	_log.finest("byte -> end");
            	return Byte.valueOf(bb.get());
                
            } else if (type == char.class) {
            	
            	_log.finest("char -> end");
            	return Character.valueOf(bb.getChar());
                
            } else if (type == short.class) {

            	_log.finest("short -> end");
            	return Short.valueOf(bb.getShort());
                
            } else if (type == int.class) {
            	
            	_log.finest("int -> end");
            	return Integer.valueOf(bb.getInt());
                
            } else if (type == long.class) {
            	
            	_log.finest("long -> end");
            	return Long.valueOf(bb.getLong());
                
            } else if (type == float.class) {

            	_log.finest("float -> end");
            	return Float.valueOf(bb.getFloat());
                
            } else if (type == double.class) {
            	
            	_log.finest("double -> end");
            	return Double.valueOf(bb.getDouble());
                
            } else {
            	_log.finest("unknown -> end");
                throw new IOException("Unknown primitive: " + type);
            }
        	
        } else {
        	
        	_log.finest("non primitive object -> end");
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
    	
    	// tests showed that the simplest object has at least 28 bytes
    	// so we prepare for at least this size
    	ByteArrayOutputStream2 baos = new ByteArrayOutputStream2(28);
    	
		ObjectOutputStream oos = new ObjectOutputStream(baos);
	
		oos.writeObject(object);
		oos.flush();
		
		return baos.getBuf();
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
    	
    	// TODO optimization tips, see: http://www.theserverside.com/discussions/thread.tss?thread_id=21568
    	
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
	public static String getChannelString(SelectableChannel channel) {
		StringBuffer sb = new StringBuffer();

		String ip = "<unknown>";
		int remotePort = -1;
		int localPort = -1;

		
		if (channel instanceof ServerSocketChannel) {
			ip = ((ServerSocketChannel) channel).socket().getInetAddress().getHostAddress();
			localPort = ((ServerSocketChannel) channel).socket().getLocalPort();
			remotePort=0;
			
			sb.append("[server|listenOn=");
			sb.append(ip);
			sb.append(",localport=");
			sb.append(localPort);
			sb.append(",remoteport=");
			sb.append(remotePort);
			sb.append("]");
		}
		else
		if (channel instanceof SocketChannel) {
			ip = ((SocketChannel) channel).socket().getInetAddress().getHostAddress();
			localPort = ((SocketChannel) channel).socket().getLocalPort();
			remotePort= ((SocketChannel) channel).socket().getPort();
			
			sb.append("[client|connectedTo=");
			sb.append(ip);
			sb.append(",localport=");
			sb.append(localPort);
			sb.append(",remoteport=");
			sb.append(remotePort);
			sb.append("]");
		}
		
		return sb.toString();
	}
	
	/**
	 * Gets a String representation for a <Code>SelectionKey>/code>
	 * TODO: Documentation to be done for method 'getKeyString', by 'ACHR'..
	 * 
	 * @param key
	 * @return
	 */
	public static String getKeyString(SelectionKey key){
		StringBuffer sb = new StringBuffer();
		
		sb.append("[");
		sb.append(getChannelString(key.channel()));
		if (key.isValid()) {
			sb.append("interestOps=");
			sb.append(Utils.getSelectionKeyString(key.interestOps()));
			sb.append(",readyOps=");
			sb.append(Utils.getSelectionKeyString(key.readyOps()));
		}
		sb.append("]");
		
		return sb.toString();
	}

	public static String inspectPacket(ByteBuffer buf) {
		int position = buf.position();
		byte headerId0=buf.get();
		byte headerId1=buf.get();
		byte type = buf.get();
		int requestID = buf.getInt();
		int packetLength = buf.getInt();
		
		StringBuffer sb = new StringBuffer();
		sb.append("[packet|type=");
		sb.append(getPacketTypeAsString(type));
		sb.append(", headerId0=0x");
		sb.append(Integer.toHexString(headerId0));
		sb.append(", headerId1=0x");
		sb.append(Integer.toHexString(headerId1));
		sb.append(", requestID=");
		sb.append(requestID);
		sb.append(", length=");
		sb.append(packetLength);
		sb.append("]");
		
		sb.append("\n");
			
		for (int i = 0; i < packetLength; i++) {
			byte c = buf.get();;
			
			sb.append("\tbody b[");
			sb.append(i);
			sb.append("]=");
			sb.append(c);
			sb.append("\n");

		}
		
		buf.position(position);
		
		return sb.toString();
		
	}

	public static Object getPacketTypeAsString(byte type) {
		switch (type) {
			case Statics.LOOKUP_PACKET :
				return "LOOKUP_PACKET";
				
			case Statics.LOOKUP_RETURN_PACKET :
				return "LOOKUP_RETURN_PACKET";

			case Statics.EQUALS_PACKET :
				return "EQUALS_PACKET";
			
			case Statics.EQUALS_RETURN_PACKET :
				return "EQUALS_RETURN_PACKET";
				
			case Statics.HASHCODE_PACKET :
				return "HASHCODE_PACKET";

			case Statics.HASHCODE_RETURN_PACKET :
				return "HASHCODE_RETURN_PACKET";

			case Statics.INVOCATION_PACKET :
				return "INVOCATION_PACKET";
				
			case Statics.INVOCATION_RETURN_PACKET :
				return "INVOCATION_RETURN_PACKET";				

			case Statics.PING_PACKET :
				return "PING_PACKET";

			case Statics.PONG_PACKET :
				return "PONG_PACKET";

			case Statics.TOSTRING_PACKET :
				return "TOSTRING_PACKET";

			case Statics.TOSTRING_RETURN_PACKET :
				return "TOSTRING_RETURN_PACKET";
				
			default :
				return "UNKNOWN(0x"+Integer.toHexString(type)+")";
		}
	}
}
