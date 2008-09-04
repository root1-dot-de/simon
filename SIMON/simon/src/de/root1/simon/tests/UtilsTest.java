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
package de.root1.simon.tests;

import java.io.IOException;
import java.io.NotSerializableException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import de.root1.simon.Registry;
import de.root1.simon.RxPacket;
import de.root1.simon.Simon;
import de.root1.simon.Statics;
import de.root1.simon.TxPacket;
import de.root1.simon.exceptions.NameBindingException;
import de.root1.simon.tests.server.ServerInterfaceImpl;
import de.root1.simon.utils.Utils;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import javax.lang.model.element.TypeParameterElement;

/**
 * TODO document me
 * 
 * @author ACHR
 * 
 */
public class UtilsTest extends TestCase {

	private ServerInterfaceImpl serverImpl = new ServerInterfaceImpl();
	private Registry registry;
	
	public UtilsTest(String name) {
		super(name);
	}

	// initial for each test setup
	protected void setUp() {
	}

	// tear down after each test
	protected void tearDown() {
	}

	// -----------------------
	// TESTS
	// -----------------------

	public void testWrapUnwrapHashtable() {
		
		
		Hashtable<String, String> myHashTable = new Hashtable<String, String>();
		
		for (int i=0;i<100;i++) {
			myHashTable.put("myKey"+i, "myValue"+i);
		}
		
		TxPacket txPacket = new TxPacket();
		txPacket.setHeader(Statics.INVOCATION_PACKET, 0);
		
		try {
			Utils.wrapValue(Hashtable.class, myHashTable, txPacket);
		} catch (NotSerializableException e) {
			new AssertionError("HashTable is serializeable!");
		} catch (IOException e) {
			new AssertionError("wrapping a HashTable must be possible!");
		}
		
		txPacket.setComplete();
		
		ByteBuffer byteBuffer = txPacket.getByteBuffer();
		
		int simonPacketHeaderId0 = byteBuffer.get();
		int simonPacketHeaderId1 = byteBuffer.get();
		
		byte msgType = byteBuffer.get();
		int requestID = byteBuffer.getInt();
		int bodySize = byteBuffer.getInt();
		
		try {
			Hashtable<String, String> unwrappedTable = (Hashtable<String, String>)Utils.unwrapValue(Hashtable.class, byteBuffer);
			
			assertTrue("after unwrapping, the table should have the same element count", myHashTable.size()==unwrappedTable.size());
			
			int elementsEqual = 0;
			
			Iterator<String> iterator = myHashTable.keySet().iterator();
			while (iterator.hasNext()){
				String key = iterator.next();
				if (unwrappedTable.containsKey(key) && unwrappedTable.get(key).equals(myHashTable.get(key))) {
					elementsEqual++;
				}
			}
			
			assertTrue("after unwrapping, the table should have the same key/value pairs.",elementsEqual==myHashTable.size());
			
		} catch (IOException e) {
			new AssertionError("Unwrapping a Hashtable should not lead to an IOException.");
		} catch (ClassNotFoundException e) {
			new AssertionError("Unwrapping a Hashtable should not lead to an ClassNotFoundException.");
		}

	}
	


};