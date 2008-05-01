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
package de.root1.simon;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.logging.Level;

import de.root1.simon.utils.Utils;


public class LookupTable {
	
	/**
	 * Maps the remote object name to the remoteobject
	 */
	private HashMap<String, SimonRemote> bindings = new HashMap<String, SimonRemote>();
	
	/**
	 * Maps the remoteobject to the map with the hash-mapped methods.
	 */
	private HashMap<SimonRemote, HashMap<Long, Method>> simonRemoteTo_hashToMethod_Map = new HashMap<SimonRemote, HashMap<Long, Method>>();
	
	/**
	 * 
	 * TODO Documentation to be done
	 * @param name
	 * @param remoteObject
	 */
	public synchronized void putRemoteBinding(String name, SimonRemote remoteObject) {
		Utils.logger.fine("begin");
		
		if (Utils.logger.isLoggable(Level.FINER))
			Utils.logger.finer("name="+name+"  object="+remoteObject);
		
		bindings.put(name,remoteObject);	
		
		simonRemoteTo_hashToMethod_Map.put(remoteObject, computeMethodHashMap(remoteObject.getClass()));
		Utils.logger.fine("end");		
	}
	
	/**
	 * 
	 * TODO Documentation to be done
	 * @param name
	 * @return
	 */
	public synchronized SimonRemote getRemoteBinding(String name){
		Utils.logger.fine("begin");
		if (!bindings.containsKey(name)) throw new IllegalArgumentException("Lookuptable.getBinding(): name="+name+" not found");

		if (Utils.logger.isLoggable(Level.FINER))
			Utils.logger.finer("name="+name+" resolves to object='"+bindings.get(name)+"'");
		
		Utils.logger.fine("end");
		return bindings.get(name);
	}

	/**
	 * 
	 * TODO Documentation to be done
	 * @param name
	 */
	public synchronized void releaseRemoteBinding(String name){
		Utils.logger.fine("begin");

		if (Utils.logger.isLoggable(Level.FINER))
			Utils.logger.finer("name="+name);

		bindings.remove(name);
		Utils.logger.fine("end");
	}
	
	/**
	 * 
	 * TODO Documentation to be done
	 * @param remoteObject
	 * @param methodHash
	 * @return
	 */
	public synchronized Method getMethod(SimonRemote remoteObject, long methodHash){
		Utils.logger.fine("begin");
		
		if (Utils.logger.isLoggable(Level.FINER))
			Utils.logger.finer("hash="+methodHash+" resolves to method='"+simonRemoteTo_hashToMethod_Map.get(remoteObject).get(methodHash)+"'");

		Utils.logger.fine("end");
		return simonRemoteTo_hashToMethod_Map.get(remoteObject).get(methodHash);
	}

	/**
	 * 
	 * TODO Documentation to be done
	 * @param remoteObject
	 * @param methodHash
	 * @return
	 */
	public synchronized Method getMethod(String remoteObject, long methodHash){
		Utils.logger.fine("begin");

		if (Utils.logger.isLoggable(Level.FINER))
			Utils.logger.finer("hash="+methodHash+" resoves to method='"+simonRemoteTo_hashToMethod_Map.get(bindings.get(remoteObject)).get(methodHash)+"'");
		
		Utils.logger.fine("end");
		return simonRemoteTo_hashToMethod_Map.get(bindings.get(remoteObject)).get(methodHash);
	}
	
	/**
	 * 
	 * TODO Documentation to be done
	 * @param remoteClass
	 * @return
	 */
	protected HashMap<Long,Method> computeMethodHashMap(Class<?> remoteClass) {
		Utils.logger.fine("begin");

		if (Utils.logger.isLoggable(Level.FINER))
			Utils.logger.finer("computing for remoteclass='"+remoteClass+"'");

        HashMap<Long,Method> map = new HashMap<Long,Method>();
        
        for (Class<?> cl = remoteClass; cl != null; cl = cl.getSuperclass()) {

        	if (Utils.logger.isLoggable(Level.FINEST))
    			Utils.logger.finest("examin superclass='"+cl+"' for interfaces");
        	
            for (Class<?> intf : cl.getInterfaces()) {
            	
            	if (Utils.logger.isLoggable(Level.FINEST))
        			Utils.logger.finest("examin superclass' interface='"+intf+"'");

            	if (SimonRemote.class.isAssignableFrom(intf)) {

                	if (Utils.logger.isLoggable(Level.FINEST))
            			Utils.logger.finest("SimonRemote is assignable from '"+intf+"'");
                	
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
                    	if (Utils.logger.isLoggable(Level.FINEST))
                			Utils.logger.finest("computing hash: method='"+m+"' hash="+Utils.computeMethodHash(m));

                    }
                }
            } 
        }
        
		Utils.logger.fine("begin");
        return map;
    }

}
