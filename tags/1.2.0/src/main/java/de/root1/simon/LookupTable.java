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

import de.root1.simon.exceptions.LookupFailedException;
import de.root1.simon.utils.Utils;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * This class is "the brain" of SIMON. It saves all known remote object <-> name
 * relations, as well as hashcodes for all the methods in the remote object. If
 * a object is getting unreferenced over the network connection, it gets
 * "informed" by the
 * <code>unreferenced()</code> method, if {@link SimonUnreferenced} is
 * implemented.
 *
 * @author ACHR
 *
 */
public class LookupTable implements LookupTableMBean {

    /**
     * the local logger
     */
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    /**
     * Maps the remote object name to the remote object. Only Objects wich have
     * been registered with the Registry.bind() method are added to this map.
     *
     */
    private final HashMap<String, RemoteObjectContainer> bindings = new HashMap<String, RemoteObjectContainer>();
    
    /**
     * A Map that holds a list of remote object names for each socket connection.
     * The names are used to clean up upon DGC / session close
     *
     * <pre>
     * &lt;Session-ID, List&lt;remoteObjectName&gt;&gt;
     * </pre>
     */
    private final Map<Long, List<String>> gcRemoteInstances = new HashMap<Long, List<String>>();
    
    /**
     * Maps the remote object to the map with the hash-mapped methods.
     */
    private final Map<Object, Map<Long, Method>> remoteObject_to_hashToMethod_Map = new HashMap<Object, Map<Long, Method>>();
    
    /**
     * Set with remote object instances. Used to identify already registered
     * remote objects.
     * The same information is also available in "bindings", but query would be complexer/more time consuming.
     *
     * @since 1.2.0
     */
    private final Set<Object> remoteobjectSet = new HashSet<Object>();
    
    /**
     * Container for callback references
     * <pre>
     * &lt;sessionId, &lt;refId, RemoteRefContainer&gt;&gt;
     * </pre>
     */
    private final Map<Long, Map<String, RemoteRefContainer>> sessionRefCount = new HashMap<Long, Map<String, RemoteRefContainer>>();
    
    
    private Dispatcher dispatcher;
    private boolean cleanupDone = false;

    /**
     * Called via Dispatcher to create a lookup table. There's only one
     * LookupTable for one Dispatcher.
     *
     * @param dispatcher
     */
    protected LookupTable(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
        Simon.registerLookupTable(this);
           
        String objectNameOfMBean = "de.root1.simon:"
                + "type="+MBEAN_TYPE
                + ","
                + "subType="+(dispatcher.getServerString()==null?MBEAN_SUBTYPE_SERVER:MBEAN_SUBTYPE_CLIENT)
                + ","
                + "instance="+MBEAN_TYPE+"@"+hashCode();
        Utils.registerMBean(this, objectNameOfMBean);
    }
    
    /**
     * Saves a remote object in the lookup table for later reference
     *
     * @param remoteObjectName the name of the remote object
     * @param remoteObject a simon remote object
     */
    synchronized void putRemoteBinding(String remoteObjectName, Object remoteObject) {
        logger.debug("begin");

        logger.debug("remoteObjectName={} object={}", remoteObjectName, remoteObject);

        addRemoteObjectToSet(remoteObject);
        
        RemoteObjectContainer roc = new RemoteObjectContainer(remoteObject, remoteObjectName, remoteObject.getClass().getInterfaces());
        bindings.put(remoteObjectName, roc);

        logger.debug("Put {} to remoteObject_to_hashToMethod_Map", remoteObject);
        Map<Long, Method> put = remoteObject_to_hashToMethod_Map.put(remoteObject, computeMethodHashMap(remoteObject.getClass()));
        if (put!=null) {
            logger.error("remoteobject {} already existed int remoteObject_to_hashToMethod_Map");
        }
        logger.debug("end");
    }

    /**
     * Stores remote objects. Normally it wouldn't be required to store the
     * remote objects in a separate set/map, but bindings contains
     * (remoteobjectname/RemoteObjectContainer) pairs, which makes search by
     * object complexer/slower.
     *
     * @param remoteObject
     */
    private void addRemoteObjectToSet(Object remoteObject) {
        int hashCode = remoteObject.hashCode();
        remoteobjectSet.add(remoteObject);
        logger.trace("Adding remote object {} with hash={}", remoteObject, hashCode);
    }

    
    /**
     * Adds a callback reference to the internal reference storage
     * @param sessionId the related session id
     * @param refId the reference if for the object to reference
     * @param object the object to reference
     */
    private void addCallbackRef(long sessionId, String refId, Object object) {
     
        logger.debug("Adding {}", refId);
        synchronized(sessionRefCount) {
            Map<String, RemoteRefContainer> sessionMap = sessionRefCount.get(sessionId);
            
            if (sessionMap==null) {
                // session not yet known, creating new ref container
                sessionMap = new HashMap<String, RemoteRefContainer>();
                sessionMap.put(refId, new RemoteRefContainer(object));
                logger.debug("Added RefCounter for {}. {}", refId, toString());
                sessionRefCount.put(sessionId, sessionMap);
            } else {
                RemoteRefContainer ref = sessionMap.get(refId);
                if (ref==null) {
                    // session known, but no ref container yet
                    ref = new RemoteRefContainer(object);
                    sessionMap.put(refId, ref);
                } else {
                    // session+ref known, increase ref counter
                    ref.addRef();
                }
                logger.debug("RefCount for {} is now: ", refId, ref.getRefCount());
                
            }
        }
    }
    
    /**
     * Removes a callback reference from the internal reference storage
     * @param sessionId the related session id
     * @param refId the reference id of the callback
     */
    synchronized void removeCallbackRef(long sessionId, String refId) {
     
        logger.debug("Releasing {}", refId);
        synchronized(sessionRefCount) {
            
            Map<String, RemoteRefContainer> sessionMap = sessionRefCount.get(sessionId);
            
            if (sessionMap==null) {
                logger.debug("Session {} has no refs available. Something went wrong! Ref to release: {}", Utils.longToHexString(sessionId), refId);
            } else {
                RemoteRefContainer ref = sessionMap.get(refId);
                
                if (ref!=null) {
                    int oldCount = ref.getRefCount();;
                    int newCount = ref.removeRef();
                    
                    logger.debug("new count for ref {} is: {}; was: {}", new Object[]{refId, newCount, oldCount});
                    
                    if (newCount==0) {
                        sessionMap.remove(refId);
                        
                        logger.trace("session map now contains {} items", sessionMap.size());
                        if (sessionMap.isEmpty()) {
                            sessionRefCount.remove(sessionId);
                            logger.trace("{} sessions have references", sessionRefCount.size());
                        }
                    }
                } else {
                    logger.warn("Something went wrong: ref {} not found in sessionmap on session {}", refId, Utils.longToHexString(sessionId));
                }
            }
            releaseRemoteBinding(refId);
            synchronized (gcRemoteInstances) {
                List<String> list = gcRemoteInstances.get(sessionId);
                if(list!=null) {
                    boolean remove = list.remove(refId);
                    logger.debug("Removed {} from list of gcRemoteInstance for session {}", refId, sessionId);
                }
            }
        }
    }
    
    
    /**
     * This method is used by the {@link Dispatcher} and the
     * {@link ProcessMessageRunnable} class when sending a
     * {@link SimonRemoteInstance}. Calling this method will store the simon
     * remote instance for later GC along with the session. This is necessary
     * for the DGC to release all remote instances which are related to a
     * specific {@link IoSession}. The remote instance is also stored as a
     * remote binding.
     *
     * @param sessionId the id from {@link IoSession#getId()} from the related
     * {@link IoSession}
     * @param simonRemoteInstance the related SimonRemoteInstance
     * @param remoteObject the remote object that has been found in a method
     * argument or method result
     */
    synchronized void putRemoteInstance(long sessionId, SimonRemoteInstance simonRemoteInstance, Object remoteObject) {
        logger.debug("begin");

        String sriRemoteObjectName = simonRemoteInstance.getId();

        logger.debug("sessionId={} sriRemoteObjectName={} remoteObject=", new Object[]{Utils.longToHexString(sessionId), sriRemoteObjectName, remoteObject});

        addCallbackRef(sessionId, sriRemoteObjectName, remoteObject);
        
        // list ob remote object names that need to be GC'ed somewhen later
        List<String> remoteObjectNames;
        
        // if there no list present, create one
        if (!gcRemoteInstances.containsKey(sessionId)) {
            logger.debug("session '{}' unknown, creating new remote instance list!", Utils.longToHexString(sessionId));
            remoteObjectNames = new ArrayList<String>();
            gcRemoteInstances.put(sessionId, remoteObjectNames);
        } else {
            remoteObjectNames = gcRemoteInstances.get(sessionId);
        }
        /*
         * if remote is not already known, add it to list
         * This check is useful when you provide one and the same callback object to server many times.
         * There the name is always the same. And when unreferencing the object get's unreferenced once.
         */
        if (!remoteObjectNames.contains(sriRemoteObjectName)) {
            remoteObjectNames.add(sriRemoteObjectName);

            putRemoteBinding(sriRemoteObjectName, remoteObject);

            logger.debug("session '{}' now has {} entries.", Utils.longToHexString(sessionId), remoteObjectNames.size());

        } else {
            logger.debug("sriRemoteObjectName={} already known. Skipping.", sriRemoteObjectName);
        }
        logger.debug("end");
    }

    /**
     *
     * Gets a already bind remote object according to the given remote object
     * name
     *
     * @param remoteObjectName the name of the object we are interested in
     * @return the remote object container
     * @throws LookupFailedException if remote object is not available in lookup
     * table
     */
    RemoteObjectContainer getRemoteObjectContainer(String remoteObjectName) throws LookupFailedException {
        logger.debug("begin");
        synchronized (bindings) {
            if (!bindings.containsKey(remoteObjectName)) {
                logger.debug("remote object name=[{}] not found in LookupTable!", remoteObjectName);
                throw new LookupFailedException("remoteobject with name [" + remoteObjectName + "] not found in lookup table.");
            }

            logger.debug("name={} resolves to object='{}'", remoteObjectName, bindings.get(remoteObjectName));

            logger.debug("end");
            return bindings.get(remoteObjectName);
        }
    }

    /**
     *
     * Frees a saved remote object. After a remote object is freed, it cannot be
     * looked up again until it's bound again.
     *
     * @param name the remote object to free
     */
    synchronized void releaseRemoteBinding(String name) {
        
        logger.debug("begin");
        logger.debug("name={}", name);

        synchronized (bindings) {
            RemoteObjectContainer remoteObjectContainer = bindings.remove(name);

            // remoteObject may be null in case of multithreaded access
            // to Simon#unbind() and thus releaseRemoteBinding()
            if (remoteObjectContainer != null) {
                Object remoteObject = remoteObjectContainer.getRemoteObject();
                logger.debug("cleaning up [{}]", remoteObject);
                removeRemoteObjectFromSet(remoteObject);
                logger.debug("Removing {} from remoteObject_to_hashToMethod_Map", remoteObject);
                Map<Long, Method> remove = remoteObject_to_hashToMethod_Map.remove(remoteObject);
                if (remove==null) {
                    logger.error("Object {} NOT removed from remoteObject_to_hashToMethod_Map. ROC={}", remoteObject, remoteObjectContainer);
                }
            } else {
                logger.debug("[{}] already removed or not available. nothing to do.", name);
            }
        }

        logger.debug("end");
    }

    /**
     * Removes the given object from the set of remote objects
     *
     * @param remoteObject object to remove
     */
    private void removeRemoteObjectFromSet(Object remoteObject) {
        int hashCode = remoteObject.hashCode();
        logger.debug("remoteObject={} hash={} map={}", new Object[]{remoteObject, hashCode, remoteobjectSet});
        boolean removed = remoteobjectSet.remove(remoteObject);
        assert removed;
        if (!removed) {
            logger.error("Object NOT removed!");
        }
        logger.trace("Removed remote object {} with hash={}; removed={}", new Object[]{remoteObject, hashCode, removed});
    }

    /**
     *
     * Gets a method according to the given remote object name and method hash
     * value
     *
     * @param remoteObject the remote object which contains the method
     * @param methodHash the hash of the method
     * @return the method
     */
    public synchronized Method getMethod(String remoteObject, long methodHash) {
        
        logger.debug("begin");
        
        Method m = remoteObject_to_hashToMethod_Map.get(bindings.get(remoteObject).getRemoteObject()).get(methodHash);

        logger.debug("hash={} resolves to method='{}'", methodHash, m);
        logger.debug("end");

        return m;
    }

    /**
     *
     * Computes for each method of the given remote object a method has and save
     * this in an internal map for later lookup
     *
     * @param remoteClass the class that contains the methods
     * @return a map that holds the methods hash as the key and the method
     * itself as the value
     */
    private HashMap<Long, Method> computeMethodHashMap(Class<?> remoteClass) {
        logger.debug("begin");

        logger.debug("computing for remoteclass='{}'", remoteClass);

        HashMap<Long, Method> map = new HashMap<Long, Method>();

        for (Class<?> cl = remoteClass; cl != null; cl = cl.getSuperclass()) {

            logger.debug("examin superclass='{}' for interfaces", cl);

            for (Class<?> intf : cl.getInterfaces()) {

                logger.debug("examin superclass' interface='{}'", intf);

                for (Method method : intf.getMethods()) {

                    final Method m = method;
                    /*
                     * Set this Method object to override language
                     * access checks so that the dispatcher can invoke
                     * methods from non-public remote interfaces.
                     */
                    AccessController.doPrivileged(
                            new PrivilegedAction<Void>() {
                                @Override
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

        logger.debug("begin");
        return map;
    }

    /**
     * Clears the whole {@link LookupTable}
     *
     */
    void cleanup() {
        logger.debug("begin");
        Simon.unregisterLookupTable(this);

        Iterator<Long> iterator = gcRemoteInstances.keySet().iterator();
        while (iterator.hasNext()) {
            unreference(iterator.next());
        }

        sessionRefCount.clear();
        
        bindings.clear();
        remoteObject_to_hashToMethod_Map.clear();
        sessionRefCount.clear();
        cleanupDone = true;
        logger.debug("end");
    }

    /**
     * Removes remote instance objects from {@link LookupTable}. If the remote
     * object implements the interface {@link SimonUnreferenced}, the
     * {@link SimonUnreferenced#unreferenced()} method is finally called.
     *
     * @param sessionId the id from {@link IoSession#getId()} from the related
     * {@link IoSession}
     */
    void unreference(long sessionId) {
        String id = Utils.longToHexString(sessionId);
        logger.debug("begin. sessionId={} cleanupDone={}", id, cleanupDone);

        List<String> list;
        synchronized (gcRemoteInstances) {
            list = gcRemoteInstances.remove(sessionId);
        }
        synchronized (sessionRefCount) {
            sessionRefCount.remove(sessionId);
        }

        if (list != null) {

            if (logger.isDebugEnabled()) {
                logger.debug("sessionId={} There are {} remote instances to be unreferenced.", id, list.size());
            }

            for (String remoteObjectName : list) {

                if (logger.isDebugEnabled()) {
                    logger.debug("sessionId={} Unreferencing: {}", id, remoteObjectName);
                }

                synchronized (bindings) {
                    RemoteObjectContainer container = bindings.remove(remoteObjectName);
                    logger.debug("sessionId={} RemoteObjectContainer to unreference: {}", id, container);

                    if (container!=null) {
                        Object remoteInstanceBindingToRemove = container.getRemoteObject();
                        logger.debug("sessionId={} simon remote to unreference: {}", id, remoteInstanceBindingToRemove);

                        removeRemoteObjectFromSet(remoteInstanceBindingToRemove);

                        remoteObject_to_hashToMethod_Map.remove(remoteInstanceBindingToRemove);

                        if (remoteInstanceBindingToRemove instanceof SimonUnreferenced) {

                            final SimonUnreferenced remoteBinding = (SimonUnreferenced) remoteInstanceBindingToRemove;
                            remoteBinding.unreferenced();

                            logger.debug("sessionId={} Called the unreferenced() method on {}", id, remoteInstanceBindingToRemove);

                        }
                    } else {
                        logger.debug("Container for {} no longer present?", remoteObjectName);
                    }
                }
            }
        }
        logger.debug("end. sessionId={} ", id);
    }

    /**
     * Returns the related Dispatcher
     *
     * @return related dispatcher
     */
    Dispatcher getDispatcher() {
        return dispatcher;
    }

    /**
     * Checks whether the provided object is registered in the remote object
     * hashmap
     *
     * @param remoteObject
     * @return true, if the given object is registered, false if not
     */
    boolean isSimonRemoteRegistered(Object remoteObject) {
        if (remoteObject == null) {
            return false;
        }
        logger.trace("searching hash {} in {}", remoteObject.hashCode(), remoteobjectSet);
        if (remoteobjectSet.contains(remoteObject)) {
            return true;
        }
        return false;
    }

    /**
     * Gets a already bind remote object according to the given remote interface
     * name
     *
     * @param interfaceName then name of the interface to query for
     * @return the corresponding <code>RemoteObjectContainer</code>
     * @throws LookupFailedException if nothing was found, or if the found
     * result is not unique
     */
    synchronized RemoteObjectContainer getRemoteObjectContainerByInterface(String interfaceName) throws LookupFailedException {
        RemoteObjectContainer foundContainer = null;

        // Iterate over all bindings to find an remote object that implements the searched interface
        for (String remoteObjectName : bindings.keySet()) {

            RemoteObjectContainer knownContainer = bindings.get(remoteObjectName);

            for (Class<?> interfaze : knownContainer.getRemoteObjectInterfaces()) {

                if (interfaze.getName().equals(interfaceName)) {

                    // check uniqueness of container
                    if (foundContainer == null) {
                        foundContainer = knownContainer;
                    } else {
                        if (foundContainer.getRemoteObject() != knownContainer.getRemoteObject()) {
                            throw new LookupFailedException("No unique '" + interfaceName + "' interface implementation found in bindings.");
                        }
                    }
                }
            }
        }

        if (foundContainer == null) {
            throw new LookupFailedException("No '" + interfaceName + "' interface implementation found");
        }

        return foundContainer;
    }
    
    /* *************************************
     *              JMX Stuff
     * *************************************/
    
    @Override
    public int getNumberOfRemoteRefSessions() {
        logger.debug("{}", toString());
        return sessionRefCount.size();
    }

    @Override
    public Long[] getRemoteRefSessions() {
        return sessionRefCount.keySet().toArray(new Long[0]);
    }

    @Override
    public String[] getRefIdsForSession(long sessionId) {
        return sessionRefCount.get(sessionId).keySet().toArray(new String[0]);
    }
    
    @Override
    public int getRemoteRefCount(long sessionId, String refId) {
        return sessionRefCount.get(sessionId).get(refId).getRefCount();
    }
    
    @Override
    public int getTotalRefCount() {
        int i=0;
        synchronized(sessionRefCount) {
            Iterator<Long> sessionIter = sessionRefCount.keySet().iterator();
            while (sessionIter.hasNext()) {
                Long sessionId = sessionIter.next();
                Map<String, RemoteRefContainer> refMap = sessionRefCount.get(sessionId);
                Collection<RemoteRefContainer> values = refMap.values();
                for (RemoteRefContainer remoteRef : values) {
                    i += remoteRef.getRefCount();
                }
            }
        }
        return i;
    }
    
    @Override
    public List<String> getCallbackRefList() {
        
        List<String> list = new ArrayList<String>();
        
        synchronized(sessionRefCount) {
            Iterator<Long> sessionIter = sessionRefCount.keySet().iterator();
            while (sessionIter.hasNext()) {
                Long sessionId = sessionIter.next();
                Map<String, RemoteRefContainer> refMap = sessionRefCount.get(sessionId);
                Collection<RemoteRefContainer> values = refMap.values();
                for (RemoteRefContainer remoteRef : values) {
                    list.add("Session: "+Utils.longToHexString(sessionId)+" -> "+remoteRef.toString());
                }
            }
        }
        
        return list;
        
    }



}
