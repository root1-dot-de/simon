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
	
	public LookupTable() {
		// TODO Auto-generated constructor stub
	}
	
	public synchronized void putRemoteBinding(String name, SimonRemote remoteObject) {
		Utils.debug("LookupTable.putRemoteBinding() -> name="+name+"  object="+remoteObject);
		bindings.put(name,remoteObject);	
		
		simonRemoteTo_hashToMethod_Map.put(remoteObject, computeMethodHashMap(remoteObject.getClass()));
		
	}
	
	public synchronized SimonRemote getRemoteBinding(String name){
		if (!bindings.containsKey(name)) throw new IllegalArgumentException("Lookuptable.getBinding(): name="+name+" not found");
		Utils.debug("LookupTable.getRemoteBinding() -> name="+name+" resolves to object='"+bindings.get(name)+"'");
		return bindings.get(name);
	}
	
	public synchronized void releaseRemoteBinding(String name){
//		Utils.debug("\t\tLookupTable#releaseBinding: name="+name);
		bindings.remove(name);
	}
	
	public synchronized Method getMethod(SimonRemote remoteObject, long methodHash){
		Utils.debug("LookupTable.getMethod() -> hash="+methodHash+" resolves to method='"+simonRemoteTo_hashToMethod_Map.get(remoteObject).get(methodHash)+"'");
		return simonRemoteTo_hashToMethod_Map.get(remoteObject).get(methodHash);
	}
	
	public synchronized Method getMethod(String remoteObject, long methodHash){
		Utils.debug("LookupTable.getMethod() -> hash="+methodHash+" resoves to method='"+simonRemoteTo_hashToMethod_Map.get(bindings.get(remoteObject)).get(methodHash)+"'");
		return simonRemoteTo_hashToMethod_Map.get(bindings.get(remoteObject)).get(methodHash);
	}
	
	
	// ******************************
	
	
	protected HashMap<Long,Method> computeMethodHashMap(Class<?> remoteClass) {
		Utils.debug("LookupTable.computeMethodHashMap() -> start. computing for remoteclass='"+remoteClass+"'");
        HashMap<Long,Method> map = new HashMap<Long,Method>();
        
        for (Class<?> cl = remoteClass; cl != null; cl = cl.getSuperclass()) {
        	Utils.debug("LookupTable.computeMethodHashMap() -> examin superclass='"+cl+"' for interfaces");
        	
            for (Class<?> intf : cl.getInterfaces()) {
            	Utils.debug("LookupTable.computeMethodHashMap() -> examin superclass' interface='"+intf+"'");
                if (SimonRemote.class.isAssignableFrom(intf)) {

                	Utils.debug("LookupTable.computeMethodHashMap() -> SimonRemote is assignable from '"+intf+"'");
                	
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
                        Utils.debug("LookupTable.computeMethodHashMap() -> computing hash: method='"+m+"' hash="+Utils.computeMethodHash(m));
                    }
                }
            } 
        }
        Utils.debug("LookupTable.computeMethodHashMap() -> end");
        return map;
    }

}
