package de.root1.simon.tests;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class DirectByteBufferPool {
	
	private static DirectByteBufferPool instance;

	private HashMap<Integer, List<ByteBuffer>> pool = new HashMap<Integer, List<ByteBuffer>>();
	private LinkedList<Integer> insertedSizes = new LinkedList<Integer>();
	
	int maxPoolSize = 10000;
	
	int minSize = 0;
	int maxSize = 0;
	
	private DirectByteBufferPool() {
	}

	public static DirectByteBufferPool getInstance(){
		if (instance==null) {
			instance = new DirectByteBufferPool();
		}
		
		return instance;
	}
	
	public ByteBuffer getByteBuffer(int askedSize) {
		
		if (askedSize > 10000) {
		synchronized (pool) {
			
			if (pool.containsKey(askedSize)) {
//				log("Getting one out of the pool with size="+askedSize);

//				log("   arraylistsize before: "+pool.get(askedSize).size());
				ByteBuffer resultByteBuffer = pool.get(askedSize).remove(0);
//				log("   arraylistsize after: "+pool.get(askedSize).size());		
				
				// if there is no further ByteBuffer with the asked size in the pool, delete it's list
				if (pool.get(askedSize).size()==0) {
					pool.remove(askedSize);
				}
//				log("   inserted="+insertedSizes+"");
				
				for (int i = 0; i < insertedSizes.size(); i++) {
					if (insertedSizes.get(i)==askedSize) {
						insertedSizes.remove(i);
						break;
					}
				}
				
//				log("   inserted="+insertedSizes+"");
				resultByteBuffer.clear();
//				log("   "+resultByteBuffer);
//				log("   --> Pool size = "+insertedSizes.size()+"\n");

				
				return resultByteBuffer;
			} else {
//				log("Creating new one with size="+askedSize);
				ByteBuffer resultByteBuffer = ByteBuffer.allocateDirect(askedSize);
//				log("   inserted="+insertedSizes+"");
//				log("   "+resultByteBuffer);
//				log("   --> Pool size = "+insertedSizes.size()+"\n");
				return resultByteBuffer;
			}
			
			
		}}
		else return ByteBuffer.allocateDirect(askedSize);
	}


	public void releaseByteBuffer(ByteBuffer byteBufferToPutBack) {
		
//		log("Releasing bb --> "+byteBufferToPutBack);
		
		if (byteBufferToPutBack.isDirect()) {
			
			byteBufferToPutBack.clear();
			int size = byteBufferToPutBack.capacity();
//			log("   after clear: "+byteBufferToPutBack);
			
			synchronized (pool) {
				
				if (insertedSizes.size()<maxPoolSize) {
					
					// pool has enough space for a new entry
					
					addToPool(byteBufferToPutBack, size);
//					log("   there was enough size");
					
				} else {
					
					// pool is full, need to remove the oldest entry
					
//					log("   have to drain the pool first. insertedSizes.size()="+insertedSizes.size());
//					log("   "+insertedSizes+"");
//					log("   "+pool+"");
					
					drainPool();
					addToPool(byteBufferToPutBack, size);
					
					
				}
//				log("   "+insertedSizes+"");
//				log("   --> Pool size = "+insertedSizes.size()+"\n");
//				
			}

		} else {
			// ByteBuffer not pooled nor needed
			byteBufferToPutBack = null;
		}
	}

	private void addToPool(ByteBuffer byteBufferToPutBack, int size) {
		
		if (!pool.containsKey(size)){
//			log("   no such size cached size="+size);
			List<ByteBuffer> list = new ArrayList<ByteBuffer>();
			list.add(byteBufferToPutBack);
			pool.put(size, list);
//			log("   after adding arraylist: "+pool);
		} else {
//			log("   list already exists: size="+size);
			List<ByteBuffer> list = (ArrayList<ByteBuffer>) pool.get(size);
//		    log("   listsize before: "+list.size());
			list.add(byteBufferToPutBack);
//			log("   listsize after: "+pool.get(size).size());
		}
		
		insertedSizes.addFirst(size);
	}

	/**
	 * Remove the oldest inserted BufferSize
	 *
	 */
	private void drainPool() {
		
		// remove one from the oldest size entries
		Integer removeLast = insertedSizes.removeLast();
		
//		log("   draining last size="+removeLast+" pool.get(#)="+pool.get(removeLast));
		
		
		pool.get(removeLast).remove(0);
		
		// if there is no further ByteBuffer in the list, remove the list
		if (pool.get(removeLast).size()==0) {
			pool.remove(removeLast);
		}
	}
	
//	private void log(String msg) {
//		System.out.println(msg);
//		System.out.flush();
//	}

}
