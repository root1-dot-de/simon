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
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.mina.core.session.IoSession;

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
	
	protected transient Logger _log = Logger.getLogger(this.getClass().getName());
	
	/**
	 * Maps the remote object name to the remote object
	 */
	private HashMap<String, SimonRemote> bindings = new HashMap<String, SimonRemote>();
	
	
	/**
	 * A Map that holds a list of remote instances for each socket connection, which contains names of 
	 * remote objects which have to be removed if {@link DGC} finds a related broken connection
	 * 
	 * <Utils.getKeyIdentifier(session), List<remoteObjectName>>
	 */
	private Map<String, List<String>> gcRemoteInstances = new HashMap<String, List<String>>();
	
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
	 * This method is used by the {@link Dispatcher} and the {@link ProcessMessageRunnable} class. If a method to invoke or a invoke result is a 
	 * remote object, then this is saved in an extra map in the lookup table. This is necessary for the DGC to 
	 * release all remote instances which are related to a specific {@link IoSession}.
	 * 
	 * @param session the related IoSession
	 * @param remoteObjectName the related remote object name
	 * @param remoteObject the remote object that has been found in a method argument or method result
	 */
	public synchronized void putRemoteInstanceBinding(IoSession session, String remoteObjectName, SimonRemote remoteObject){
		_log.fine("begin");
		
		if (_log.isLoggable(Level.FINER))
			_log.finer("session="+session+" remoteObjectName="+remoteObjectName+"  remoteObject="+remoteObject);
		
		List<String> remotes;
		
		// if there no list present, create one
		if (!gcRemoteInstances.containsKey(session.toString())) {
			_log.finer("session unknown, creating new remote instance list!");
			remotes = new ArrayList<String>();
			gcRemoteInstances.put(session.toString(), remotes);
		} else {
			remotes = gcRemoteInstances.get(session.toString());
		}
		
		remotes.add(remoteObjectName);
		
		putRemoteBinding(remoteObjectName, remoteObject);
		
		if (_log.isLoggable(Level.FINEST))
			_log.finest("session="+session.toString()+" now has "+remotes.size()+" entries.");
				
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
			_log.fine("remote object name=["+name+"] not found in LookupTable!");	
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
	 * @param name the remote object to free
	 */
	public synchronized void releaseRemoteBinding(String name){
		_log.fine("begin");

		if (_log.isLoggable(Level.FINER))
			_log.finer("name="+name);

		bindings.remove(name);
		simonRemote_to_hashToMethod_Map.remove(name);
		
		_log.fine("end");
	}
	
//	/**
//	 * 
//	 * TODO Documentation to be done
//	 * @param remoteObject
//	 * @param methodHash
//	 * @return
//	 */
//	public synchronized Method getMethod(SimonRemote remoteObject, long methodHash){
//		_log.fine("begin");
//		
//		if (_log.isLoggable(Level.FINER))
//			_log.finer("hash="+methodHash+" resolves to method='"+simonRemote_to_hashToMethod_Map.get(remoteObject).get(methodHash)+"'");
//
//		_log.fine("end");
//		return simonRemote_to_hashToMethod_Map.get(remoteObject).get(methodHash);
//	}

	/**
	 * 
	 * Gets a method according to the given remote object name and method hash value
	 * 
	 * @param remoteObject the remote object which contains the method
	 * @param methodHash the hash of the method
	 * @return the method
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

	/**
	 * removes remote instance objects from {@link LookupTable}.
	 * 
	 * @param session the session which is the parent of those remote instance objects
	 */
	public void unreference(IoSession session) {
		
		_log.fine("begin");
		
		if (_log.isLoggable(Level.FINER))
			_log.finer("unreferencing session="+session);
		
		List<String> list;
		synchronized (gcRemoteInstances) {
			 list = gcRemoteInstances.remove(session);
		}
		
		if (list!=null) {
			
			if (_log.isLoggable(Level.FINER))
				_log.fine("There are "+list.size()+" remote instances to be removed.");
			
			for (String remoteObjectName : list) {
				
				if (_log.isLoggable(Level.FINER))
					_log.fine("Removing: "+remoteObjectName);
				
				synchronized (bindings) {
					
					SimonRemote remoteInstanceBindingToRemove = bindings.remove(remoteObjectName);
					simonRemote_to_hashToMethod_Map.remove(remoteInstanceBindingToRemove);
					
					if (remoteInstanceBindingToRemove instanceof SimonUnreferenced) {
						
						final SimonUnreferenced remoteBinding = (SimonUnreferenced) remoteInstanceBindingToRemove; 
						remoteBinding.unreferenced();
						
						if (_log.isLoggable(Level.FINER))
							_log.fine("Called the unreferenced() method on ["+remoteInstanceBindingToRemove+"]");

					}
				}
			}
		}
		_log.fine("end");
	}

}
