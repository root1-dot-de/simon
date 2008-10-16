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
import java.nio.ByteBuffer;
import java.util.Hashtable;
import java.util.Iterator;

import junit.framework.TestCase;
import de.root1.simon.Statics;
import de.root1.simon.TxPacket;
import de.root1.simon.utils.Utils;


/**
 * TODO document me
 * 
 * @author ACHR
 * 
 */
public class UtilsTest extends TestCase {

	public UtilsTest(String name) {
		super(name);
	}

	// initial for each test setup
	protected void setUp() {
		Utils.DEBUG = true;
	}

	// tear down after each test
	protected void tearDown() {
		Utils.DEBUG = false;
	}

	// -----------------------
	// TESTS
	// -----------------------

	@SuppressWarnings("unchecked")
	public void testWrapUnwrapHashtable() {
		
		
		Hashtable<String, String> myHashTable = new Hashtable<String, String>();
		
		for (int i=0;i<100;i++) {
			myHashTable.put("myKey"+i, "myValue"+i);
		}
		
		TxPacket txPacket = new TxPacket();
		txPacket.setHeader(Statics.INVOCATION_PACKET, 0);
		
		try {
			Utils.wrapValue(Hashtable.class, myHashTable, txPacket);
			Utils.wrapValue(Hashtable.class, myHashTable, txPacket);
		} catch (NotSerializableException e) {
			new AssertionError("HashTable is serializeable!");
		} catch (IOException e) {
			new AssertionError("wrapping a HashTable must be possible!");
		}
		
		txPacket.setComplete();
		
		ByteBuffer byteBuffer = txPacket.getByteBuffer();
		
		@SuppressWarnings("unused")
		int simonPacketHeaderId0 = byteBuffer.get();
		@SuppressWarnings("unused")
		int simonPacketHeaderId1 = byteBuffer.get();
		
		@SuppressWarnings("unused")
		byte msgType = byteBuffer.get();
		@SuppressWarnings("unused")
		int requestID = byteBuffer.getInt();
		@SuppressWarnings("unused")
		int bodySize = byteBuffer.getInt();
		
		try {
			Hashtable<String, String> unwrappedTable1 = (Hashtable<String, String>)Utils.unwrapValue(Hashtable.class, byteBuffer);
			Hashtable<String, String> unwrappedTable2 = (Hashtable<String, String>)Utils.unwrapValue(Hashtable.class, byteBuffer);
			
			assertTrue("after unwrapping, the 1st table should have the same element count", myHashTable.size()==unwrappedTable1.size());
			assertTrue("after unwrapping, the 2nd table should have the same element count", myHashTable.size()==unwrappedTable2.size());
			
			int elementsEqual1 = 0;
			int elementsEqual2 = 0;
			
			Iterator<String> iterator = myHashTable.keySet().iterator();
			while (iterator.hasNext()){
				String key = iterator.next();
				if (unwrappedTable1.containsKey(key) && unwrappedTable1.get(key).equals(myHashTable.get(key))) {
					elementsEqual1++;
				}
				
				if (unwrappedTable2.containsKey(key) && unwrappedTable2.get(key).equals(myHashTable.get(key))) {
					elementsEqual2++;
				}
			}
			
			assertTrue("after unwrapping, the 1st table should have the same key/value pairs. equal elements:"+elementsEqual1,elementsEqual1==myHashTable.size());
			
			assertTrue("after unwrapping, the 2nd table should have the same key/value pairs. equal elements:"+elementsEqual2,elementsEqual2==myHashTable.size());
			
		} catch (IOException e) {
			new AssertionError("Unwrapping a Hashtable should not lead to an IOException.");
		} catch (ClassNotFoundException e) {
			new AssertionError("Unwrapping a Hashtable should not lead to an ClassNotFoundException.");
		}

	}
	


};