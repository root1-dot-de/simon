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
package de.root1.simon;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.root1.simon.exceptions.LookupFailedException;
import de.root1.simon.utils.Utils;

/**
 * 
 * This class is "the brain" of SIMON. It saves all known 
 * remote object <-> name relations, as well as hashcodes 
 * for all the methods in the remote object.
 * If a object is getting unreferenced over the network connection, 
 * it gets "informed" by the <code>unreferenced()</code> method, 
 * if {@link SimonUnreferenced} is implemented.
 * 
 * @author ACHR
 *
 */
public class LookupTable {
	
	/**
	 * TODO document me
	 */
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	/**
	 * Maps the remote object name to the remote object
	 */
	private HashMap<String, SimonRemote> bindings = new HashMap<String, SimonRemote>();
	
	
	/**
	 * A Map that holds a list of remote instances for each socket connection, which contains names of 
	 * remote objects which have to be removed if DGC finds a related broken connection
	 * 
	 * <session-ID, List<remoteObjectName>>
	 */
	private Map<Long, List<String>> gcRemoteInstances = new HashMap<Long, List<String>>();
	
	/**
	 * Maps the remote object to the map with the hash-mapped methods.
	 */
	private Map<SimonRemote, Map<Long, Method>> simonRemote_to_hashToMethod_Map = new HashMap<SimonRemote, Map<Long, Method>>();

	/**
	 * TODO document me
	 */
	private HashMap<Integer, SimonRemote> remoteobjectHashMap = new HashMap<Integer, SimonRemote>();

	private Dispatcher dispatcher;

	private boolean cleanupDone = false;
	
	protected LookupTable(Dispatcher dispatcher) {
		this.dispatcher=dispatcher;
		Simon.registerLookupTable(this);
	}
	
	/**
	 * Saves a remote object in the lookup table for later reference
	 * 
	 * @param remoteObjectName the name of the remote object
	 * @param remoteObject a remote objects which implements SimonRemote
	 */
	protected synchronized void putRemoteBinding(String remoteObjectName, SimonRemote remoteObject) {
		logger.debug("begin");
		
		logger.debug("remoteObjectName={} object={}", remoteObjectName, remoteObject);
		
		addRemoteObjectToHashMap(remoteObject);
		bindings.put(remoteObjectName,remoteObject);	
		
		simonRemote_to_hashToMethod_Map.put(remoteObject, computeMethodHashMap(remoteObject.getClass()));
		logger.debug("end");		
	}

	/**
	 * TODO document me
	 * @param remoteObject
	 */
	private void addRemoteObjectToHashMap(SimonRemote remoteObject) {
		int hashCode=remoteObject.hashCode();
		remoteobjectHashMap.put(hashCode, remoteObject);
		logger.trace("Adding SimonRemote {} with hash={}", remoteObject, hashCode);
	}
	
	/**
	 * This method is used by the {@link Dispatcher} and the
	 * {@link ProcessMessageRunnable} class. If a objects is passed and is an
	 * instance of {@link SimonRemote}, then this object is saved in an extra
	 * map in the lookup table. This is necessary for the DGC to release all
	 * remote instances which are related to a specific {@link IoSession}.
	 * 
	 * @param sessionId
	 *            the id from {@link IoSession#getId()} from the related
	 *            {@link IoSession}
	 * @param remoteObjectName
	 *            the related remote object name
	 * @param remoteObject
	 *            the remote object that has been found in a method argument or
	 *            method result
	 */
	protected synchronized void putRemoteInstanceBinding(long sessionId, String remoteObjectName, SimonRemote remoteObject){
		logger.debug("begin");
		
		logger.debug("sessionId={} remoteObjectName={} remoteObject=", new Object[]{Utils.longToHexString(sessionId), remoteObjectName, remoteObject});
		
		List<String> remotes;
		
		// if there no list present, create one
		if (!gcRemoteInstances.containsKey(sessionId)) {
			logger.debug("session '{}' unknown, creating new remote instance list!", Utils.longToHexString(sessionId));
			remotes = new ArrayList<String>();
			gcRemoteInstances.put(sessionId, remotes);
		} else {
			remotes = gcRemoteInstances.get(sessionId);
		}
		
		remotes.add(remoteObjectName);
		
		putRemoteBinding(remoteObjectName, remoteObject);
		
		logger.debug("session '{}' now has {} entries.", Utils.longToHexString(sessionId), remotes.size());
				
		simonRemote_to_hashToMethod_Map.put(remoteObject, computeMethodHashMap(remoteObject.getClass()));
		logger.debug("end");		
	}
	
	
	/**
	 * 
	 * Gets a already bind remote object according to the given remote object name
	 * 
	 * @param remoteObjectName the name of the object we are interested in
	 * @return the remote object
	 * @throws LookupFailedException if remote object is not available in lookup table
	 */
	protected synchronized SimonRemote getRemoteBinding(String remoteObjectName) throws LookupFailedException {
		logger.debug("begin");
		if (!bindings.containsKey(remoteObjectName)) {
			logger.debug("remote object name=[{}] not found in LookupTable!", remoteObjectName);	
			throw new LookupFailedException("remoteobject with name ["+remoteObjectName+"] not found in lookup table.");
		}

		logger.debug("name={} resolves to object='{}'", remoteObjectName, bindings.get(remoteObjectName));
		
		logger.debug("end");
		return bindings.get(remoteObjectName);
	}

	/**
	 * 
	 * Frees a saved remote object. After a remote object is freed, it cannot be looked up again until it's bind again.
	 * 
	 * @param name the remote object to free
	 */
	protected synchronized void releaseRemoteBinding(String name){
		logger.debug("begin");

		logger.debug("name={}",name);

		SimonRemote simonRemote = bindings.remove(name); 

                // simonRemote may be null in case of multithreaded access
                // to Simon#unbind() and thus releaseRemoteBinding()
                if (simonRemote!=null) {
                    logger.debug("cleaning up [{}]");    
                    removeRemoteObjectFromHashMap(simonRemote);
                    simonRemote_to_hashToMethod_Map.remove(simonRemote);
                } else {
                    logger.debug("[{}] already removed or not available. nothing to do.");    
                }
		
		logger.debug("end");
	}

	/**
	 * TODO document me
	 * @param simonRemote
	 */
	private void removeRemoteObjectFromHashMap(SimonRemote simonRemote) {
		int hashCode=simonRemote.hashCode();
		logger.debug("simonRemote={} hash={} map={}", new Object[]{simonRemote, hashCode, remoteobjectHashMap});
		remoteobjectHashMap.remove(hashCode);
		logger.trace("Removing SimonRemote with hash={}", hashCode);
	}
	
	/**
	 * 
	 * Gets a method according to the given remote object name and method hash value
	 * 
	 * @param remoteObject the remote object which contains the method
	 * @param methodHash the hash of the method
	 * @return the method
	 */
	public synchronized Method getMethod(String remoteObject, long methodHash){
		logger.debug("begin");

		Method m = simonRemote_to_hashToMethod_Map.get(bindings.get(remoteObject)).get(methodHash);

		logger.debug("hash={} resolves to method='{}'", methodHash, m);
		logger.debug("end");
		
		return m;
	}
	
	/**
	 * 
	 * Computes for each method of the given remote object a method has and save this in an internal map for later lookup
	 * @param remoteClass the class that contains the methods
	 * @return a map that holds the methods hash as the key and the method itself as the value
	 */
	protected HashMap<Long,Method> computeMethodHashMap(Class<?> remoteClass) {
		logger.debug("begin");

		logger.debug("computing for remoteclass='{}'", remoteClass);

        HashMap<Long,Method> map = new HashMap<Long,Method>();
        
        for (Class<?> cl = remoteClass; cl != null; cl = cl.getSuperclass()) {

			logger.debug("examin superclass='{}' for interfaces", cl);
        	
            for (Class<?> intf : cl.getInterfaces()) {
            	
    			logger.debug("examin superclass' interface='{}'", intf);

            	if (SimonRemote.class.isAssignableFrom(intf)) {

        			logger.debug("SimonRemote is assignable from '{}'", intf);
                	
                    for (Method method : intf.getMethods()) {
                    	
                        final Method m = method;
                        /*
                         * Set this Method object to override language
                         * access checks so that the dispatcher can invoke
                         * methods from non-public remote interfaces.
                         */
                        AccessController.doPrivileged(
                            new PrivilegedAction<Void>() {
	                            public Void run() {
	                                m.setAccessible(true);
	                                return null;
	                            }
                            });
                        long methodHash = Utils.computeMethodHash(m);
                        map.put(methodHash, m);
            			logger.debug("computing hash: method='{}' hash={}", m, methodHash);

                    }
                }
            } 
        }
        
		logger.debug("begin");
        return map;
    }

	/**
	 * Clears the whole {@link LookupTable}
	 *
	 */
	protected void cleanup() {
		Simon.unregisterLookupTable(this);
		
		Iterator<Long> iterator = gcRemoteInstances.keySet().iterator();
		while (iterator.hasNext()){
			unreference(iterator.next());
		}
		
		bindings.clear();
		simonRemote_to_hashToMethod_Map.clear();
		cleanupDone=true;
	}

	/**
	 * Removes remote instance objects from {@link LookupTable}.
	 * If the remote object implements the interface {@link SimonUnreferenced}, 
	 * the {@link SimonUnreferenced#unreferenced()} method is finally called.
	 * 
	 * @param sessionId the id from {@link IoSession#getId()} from the related {@link IoSession}
	 */
	protected void unreference(long sessionId) {
		
		logger.debug("begin. cleanupDone={}",cleanupDone);
		
		logger.debug("unreferencing session with sessionId={}", Utils.longToHexString(sessionId));
		
		List<String> list;
		synchronized (gcRemoteInstances) {
			 list = gcRemoteInstances.remove(sessionId);
		}
		
		if (list!=null) {
			
			if (logger.isDebugEnabled())
				logger.debug("There are {} remote instances to be unreferenced.", list.size());
			
			for (String remoteObjectName : list) {
				
				if (logger.isDebugEnabled())
					logger.debug("Unreferencing: {}", remoteObjectName);
				
				synchronized (bindings) {
					
					SimonRemote remoteInstanceBindingToRemove = bindings.remove(remoteObjectName);
					
					logger.debug("SimonRemote to unreference: {}",remoteInstanceBindingToRemove);
					
					removeRemoteObjectFromHashMap(remoteInstanceBindingToRemove);
					
					simonRemote_to_hashToMethod_Map.remove(remoteInstanceBindingToRemove);
					
					if (remoteInstanceBindingToRemove instanceof SimonUnreferenced) {
						
						final SimonUnreferenced remoteBinding = (SimonUnreferenced) remoteInstanceBindingToRemove; 
						remoteBinding.unreferenced();
						
						logger.debug("Called the unreferenced() method on {}", remoteInstanceBindingToRemove);

					}
				}
			}
		}
		logger.debug("end");
	}
	
//	/**
//	 * TODO document me
//	 * @param dispatcher
//	 */
//	protected void setDispatcher(Dispatcher dispatcher){
//		this.dispatcher=dispatcher;
//	}
//	
	/**
	 * TODO document me
	 * @return
	 */
	protected Dispatcher getDispatcher(){
		return dispatcher;
	}

	/**
	 * TODO document me
	 * @param simonRemote
	 * @return
	 */
	protected boolean isSimonRemoteRegistered(SimonRemote simonRemote) {
		logger.trace("searching hash {} in {}", simonRemote.hashCode(), remoteobjectHashMap);
		if (remoteobjectHashMap.containsKey(simonRemote.hashCode())) 
			return true;
		return false;
	}

}
