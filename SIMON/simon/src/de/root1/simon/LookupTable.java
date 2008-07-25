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
import java.nio.channels.SelectionKey;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.root1.simon.exceptions.LookupFailedException;
import de.root1.simon.utils.Utils;


public class LookupTable {
	
	protected transient Logger _log = Logger.getLogger(this.getClass().getName());
	
	/**
	 * Maps the remote object name to the remote object
	 */
	private HashMap<String, SimonRemote> bindings = new HashMap<String, SimonRemote>();
	
	
	/**
	 * A Map that holds a list of remote objects for each socket connection, which contains names of 
	 * remote objects used as callbacks and which have to be removed if {@link DGC} finds a related broken connection
	 * 
	 * <"IP:LPORT:RPORT", List<remoteObjectName>>
	 */
	private Map<SelectionKey, List<String>> gcRemoteCallbacks = new HashMap<SelectionKey, List<String>>();
	
	/**
	 * Maps the remote object to the map with the hash-mapped methods.
	 */
	private Map<SimonRemote, Map<Long, Method>> simonRemote_to_hashToMethod_Map = new HashMap<SimonRemote, Map<Long, Method>>();
	
	/**
	 * Saves a remote object in the lookup table for later reference
	 * 
	 * @param name the name of the remote object
	 * @param remoteObject a remote objects which implements SimonRemote
	 */
	public synchronized void putRemoteBinding(String name, SimonRemote remoteObject) {
		_log.fine("begin");
		
		if (_log.isLoggable(Level.FINER))
			_log.finer("name="+name+"  object="+remoteObject);
		
		bindings.put(name,remoteObject);	
		
		simonRemote_to_hashToMethod_Map.put(remoteObject, computeMethodHashMap(remoteObject.getClass()));
		_log.fine("end");		
	}
	
	/**
	 * TODO name me
	 * @param key 
	 * @param name
	 * @param remoteObject
	 */
	public synchronized void putRemoteCallbackBinding(SelectionKey key, String name, SimonRemote remoteObject){
		_log.fine("begin");
		
		if (_log.isLoggable(Level.FINER))
			_log.finer("name="+name+"  object="+remoteObject);
		
		List<String> remotes;
		
		// if there no list present, create one
		if (!gcRemoteCallbacks.containsKey(key)) {
			remotes = new ArrayList<String>();
			gcRemoteCallbacks.put(key, remotes);
		} else {
			remotes = gcRemoteCallbacks.get(key);
		}
		
		remotes.add(name);
		
		putRemoteBinding(name, remoteObject);
				
		simonRemote_to_hashToMethod_Map.put(remoteObject, computeMethodHashMap(remoteObject.getClass()));
		_log.fine("end");		
	}
	
	/**
	 * 
	 * Gets a already bind remote object according to the given remote object name
	 * 
	 * @param name the name of the object we are interested in
	 * @return the remote object
	 * @throws LookupFailedException if remote object is not available in lookup table
	 */
	public synchronized SimonRemote getRemoteBinding(String name) throws LookupFailedException {
		_log.fine("begin");
		if (!bindings.containsKey(name)) {
			_log.warning("remote object name=["+name+"] not found in LookupTable!");	
			throw new LookupFailedException("remoteobject with name ["+name+"] not found in lookup table.");
		}

		if (_log.isLoggable(Level.FINER))
			_log.finer("name="+name+" resolves to object='"+bindings.get(name)+"'");
		
		_log.fine("end");
		return bindings.get(name);
	}

	/**
	 * 
	 * Frees a saved remote object. After a remote object is freed, it cannot be looked up again until it's bind again.
	 * 
	 * @param the remote object to free
	 */
	public synchronized void releaseRemoteBinding(String name){
		_log.fine("begin");

		if (_log.isLoggable(Level.FINER))
			_log.finer("name="+name);

		bindings.remove(name);
		simonRemote_to_hashToMethod_Map.remove(name);
		
		_log.fine("end");
	}
	
	/**
	 * 
	 * TODO Documentation to be done
	 * @param remoteObject
	 * @param methodHash
	 * @return
	 */
	public synchronized Method getMethod(SimonRemote remoteObject, long methodHash){
		_log.fine("begin");
		
		if (_log.isLoggable(Level.FINER))
			_log.finer("hash="+methodHash+" resolves to method='"+simonRemote_to_hashToMethod_Map.get(remoteObject).get(methodHash)+"'");

		_log.fine("end");
		return simonRemote_to_hashToMethod_Map.get(remoteObject).get(methodHash);
	}

	/**
	 * 
	 * TODO Documentation to be done
	 * @param remoteObject
	 * @param methodHash
	 * @return
	 */
	public synchronized Method getMethod(String remoteObject, long methodHash){
		_log.fine("begin");

		if (_log.isLoggable(Level.FINER))
			_log.finer("hash="+methodHash+" resolves to method='"+simonRemote_to_hashToMethod_Map.get(bindings.get(remoteObject)).get(methodHash)+"'");
		
		_log.fine("end");
		return simonRemote_to_hashToMethod_Map.get(bindings.get(remoteObject)).get(methodHash);
	}
	
	/**
	 * 
	 * Computes for each method of the given remote object a method has and save this in an internal map for later lookup
	 * @param remoteClass the class that contains the methods
	 * @return a map that holds the methods hash as the key and the method itself as the value
	 */
	protected HashMap<Long,Method> computeMethodHashMap(Class<?> remoteClass) {
		_log.fine("begin");

		if (_log.isLoggable(Level.FINER))
			_log.finer("computing for remoteclass='"+remoteClass+"'");

        HashMap<Long,Method> map = new HashMap<Long,Method>();
        
        for (Class<?> cl = remoteClass; cl != null; cl = cl.getSuperclass()) {

        	if (_log.isLoggable(Level.FINEST))
    			_log.finest("examin superclass='"+cl+"' for interfaces");
        	
            for (Class<?> intf : cl.getInterfaces()) {
            	
            	if (_log.isLoggable(Level.FINEST))
        			_log.finest("examin superclass' interface='"+intf+"'");

            	if (SimonRemote.class.isAssignableFrom(intf)) {

                	if (_log.isLoggable(Level.FINEST))
            			_log.finest("SimonRemote is assignable from '"+intf+"'");
                	
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
                        map.put(Utils.computeMethodHash(m), m);
                    	if (_log.isLoggable(Level.FINEST))
                			_log.finest("computing hash: method='"+m+"' hash="+Utils.computeMethodHash(m));

                    }
                }
            } 
        }
        
		_log.fine("begin");
        return map;
    }

	/**
	 * Clears the whole {@link LookupTable}
	 *
	 */
	public void clear() {
		bindings.clear();
		simonRemote_to_hashToMethod_Map.clear();
	}

	public void unreference(SelectionKey key) {
		List<String> list;
		synchronized (gcRemoteCallbacks) {
			 list = gcRemoteCallbacks.remove(key);
		}
		for (String remoteObjectName : list) {
			SimonUnreferenced remoteBinding;
			synchronized (bindings) {
				simonRemote_to_hashToMethod_Map.remove(remoteObjectName);
				remoteBinding = (SimonUnreferenced) bindings.remove(remoteObjectName);
			}
			remoteBinding.unreferenced();
		}
	}

}
