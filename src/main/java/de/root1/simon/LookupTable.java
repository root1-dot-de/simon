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
import java.lang.management.ManagementFactory;
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
import java.util.concurrent.atomic.AtomicInteger;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
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
     * been registered with the Registry.bin() method are added to this map.
     *
     */
    private final HashMap<String, RemoteObjectContainer> bindings = new HashMap<String, RemoteObjectContainer>();
    
    /**
     * A Map that holds a list of remote object names for each socket connection.
     * The names are used to clean up upon DGC / session close
     *
     * <Session-ID, List<remoteObjectName>>
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
//    private final Set<Object> remoteobjectSet = new HashSet<Object>();
    private final Set<Object> remoteobjectSet = new HashSet<Object>();
    
    /**
     * Stores remoteinstance-id <-> remote object pairs.<br/>
     * <code>
     * &lt;SessionID, Map&lt;ID, RemoteObject&gt;&gt;
     * </code>
     * @since 1.2.0
     */
    private final Map<Long, Map<String, Object>> remoteinstanceMap = new HashMap<Long, Map<String, Object>>();
    
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
            
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            
            ObjectName name = new ObjectName("de.root1.simon:type=LookupTable,dispatcher="+dispatcher.toString()+",isServer="+(dispatcher.getServerString()==null?"true":"false"));
            mbs.registerMBean(this, name);
            
        } catch (InstanceAlreadyExistsException ex) {
            logger.warn("This instance of LookupTable is already registerd with JMX: "+toString(), ex);
        } catch (MBeanRegistrationException ex) {
            logger.warn("This instance of LookupTable is already registerd with JMX: "+toString(), ex);
        } catch (NotCompliantMBeanException ex) {
            logger.warn("This instance of LookupTable is already registerd with JMX: "+toString(), ex);
        } catch (MalformedObjectNameException ex) {
            logger.warn("This instance of LookupTable is already registerd with JMX: "+toString(), ex);
        } catch (Throwable t) {
            /*
             * this additional "catch" is used to catch exception when running 
             * on android, where the mbean server is not available.
             */
            logger.warn("Can't use JMX.", t);
        }
        
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

        remoteObject_to_hashToMethod_Map.put(remoteObject, computeMethodHashMap(remoteObject.getClass()));
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
        logger.trace("Adding simon remote {} with hash={}", remoteObject, hashCode);
    }

//    /*
//     * FIXME
//     */
//    private void addRemoteInstanceToMap(SimonRemoteInstance sri, Object object) {
//        
//        synchronized(remoteinstanceMap) {
//            Map<String, Object> remoteInstancePair = remoteinstanceMap.get(sri.getSessionID());
//            
//            if (remoteInstancePair==null) {
//                // session not yet known
//                remoteInstancePair = new HashMap<String, Object>();
//                remoteInstancePair.put(sri.getId(), object);
//                remoteinstanceMap.put(sri.getSessionID(), remoteInstancePair);
//            } else {
//                // session already known
//                remoteInstancePair.put(sri.getId(), object);
//            }
//        }
//    }
    
//    private void removeRemoteInstanceFromMap(SimonRemoteInstance sri) {
//        synchronized(remoteinstanceMap) {
//            Map<String, Object> remoteInstancePair = remoteinstanceMap.get(sri.getSessionID());
//            if (remoteInstancePair!=null) {
//                remoteInstancePair.remove(sri.getId());
//                if (remoteInstancePair.isEmpty()) {
//                    remoteinstanceMap.remove(sri.getSessionID());
//                }
//            }
//        }
//    }
    
//    /**
//     * Returns a RemoteInstance object.
//     * 
//     * @param sessionId the session in which the object lives
//     * @param sriId the SimonRemoteInstance ID for this object
//     * @return the object
//     */
//    private Object getRemoteobjectFromRemoteInstanceMap(long sessionId, String sriId) {
//        synchronized(remoteinstanceMap) {
//            Map<String, Object> remoteInstancePair = remoteinstanceMap.get(sessionId);
//            if (remoteInstancePair!=null) {
//                Object o = remoteInstancePair.get(sriId);
//                return o;
//            }
//        }
//        return null;
//    }
    
    /**
     * Container class to count references
     */
    class RemoteRef {
        
        private final AtomicInteger refCount = new AtomicInteger(1);
        private final Object object;

        RemoteRef(Object object) {
            this.object = object;
        }

        public int getRefCount() {
            return refCount.get();
        }
        
        public int addRef() {
            return refCount.incrementAndGet();
        }
        
        public int removeRef() {
            return refCount.decrementAndGet();
        }

        public Object getObject() {
            return object;
        }
    }
    
    private final Map<Long, Map<String, RemoteRef>> sessionRefCount = new HashMap<Long, Map<String, RemoteRef>>();
    
    /*
     * FIXME
     */
    private void addCallbackRef(long sessionId, String refId, Object object) {
     
        synchronized(sessionRefCount) {
            Map<String, RemoteRef> sessionMap = sessionRefCount.get(sessionId);
            
            if (sessionMap==null) {
                // session not yet known
                sessionMap = new HashMap<String, RemoteRef>();
                sessionMap.put(refId, new RemoteRef(object));
                logger.debug("Added RefCounter for {}. {}", refId, toString());
                sessionRefCount.put(sessionId, sessionMap);
            } else {
                RemoteRef ref = sessionMap.get(refId);
                if (ref==null) {
                    ref = new RemoteRef(object);
                    sessionMap.put(refId, ref);
                } else {
                    ref.addRef();
                }
                logger.debug("RefCount for {} is now: ", refId, ref.getRefCount());
                
            }
        }
    }
    
    /*
     * FIXME
     */
    void removeCallbackRef(long sessionId, String refId) {
     
        synchronized(sessionRefCount) {
            
            releaseRemoteBinding(refId);
            Map<String, RemoteRef> sessionMap = sessionRefCount.get(sessionId);
            
            if (sessionMap==null) {
                logger.debug("No session {} has no refs available. Something went wrong! Ref to release: {}", Utils.longToHexString(sessionId), refId);
            } else {
                RemoteRef ref = sessionMap.get(refId);
                
                if (ref!=null) {
                    int newCount = ref.removeRef();
                    
                    logger.debug("new count for ref {} is: {}", refId, newCount);
                    
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
     * @return the remote object
     * @throws LookupFailedException if remote object is not available in lookup
     * table
     */
    synchronized RemoteObjectContainer getRemoteObjectContainer(String remoteObjectName) throws LookupFailedException {
        logger.debug("begin");
        if (!bindings.containsKey(remoteObjectName)) {
            logger.debug("remote object name=[{}] not found in LookupTable!", remoteObjectName);
            throw new LookupFailedException("remoteobject with name [" + remoteObjectName + "] not found in lookup table.");
        }

        logger.debug("name={} resolves to object='{}'", remoteObjectName, bindings.get(remoteObjectName));

        logger.debug("end");
        return bindings.get(remoteObjectName);
    }

    /**
     *
     * Frees a saved remote object. After a remote object is freed, it cannot be
     * looked up again until it's bind again.
     *
     * @param name the remote object to free
     */
    synchronized void releaseRemoteBinding(String name) {
        logger.debug("begin");

        logger.debug("name={}", name);

        Object remoteObject = bindings.remove(name);

        // simonRemote may be null in case of multithreaded access
        // to Simon#unbind() and thus releaseRemoteBinding()
        if (remoteObject != null) {
            logger.debug("cleaning up [{}]", remoteObject);
            removeRemoteObjectFromSet(remoteObject);
            remoteObject_to_hashToMethod_Map.remove(remoteObject);
        } else {
            logger.debug("[{}] already removed or not available. nothing to do.", name);
        }

        logger.debug("end");
    }

    /**
     * TODO document me
     *
     * @param simonRemote
     */
    private void removeRemoteObjectFromSet(Object remoteObject) {
        int hashCode = remoteObject.hashCode();
        logger.debug("remoteObject={} hash={} map={}", new Object[]{remoteObject, hashCode, remoteobjectSet});
        remoteobjectSet.remove(remoteObject);
        logger.trace("Removed remote object with hash={}", hashCode);
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
    public Method getMethod(String remoteObject, long methodHash) {
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

//                if (SimonRemote.class.isAssignableFrom(intf)) {

//                    logger.debug("SimonRemote is assignable from '{}'", intf);

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
//                }
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

                    Object remoteInstanceBindingToRemove = container.getRemoteObject();
                    logger.debug("sessionId={} simon remote to unreference: {}", id, remoteInstanceBindingToRemove);

                    removeRemoteObjectFromSet(remoteInstanceBindingToRemove);

                    remoteObject_to_hashToMethod_Map.remove(remoteInstanceBindingToRemove);

                    if (remoteInstanceBindingToRemove instanceof SimonUnreferenced) {

                        final SimonUnreferenced remoteBinding = (SimonUnreferenced) remoteInstanceBindingToRemove;
                        remoteBinding.unreferenced();

                        logger.debug("sessionId={} Called the unreferenced() method on {}", id, remoteInstanceBindingToRemove);

                    }
                }
            }
        }
        logger.debug("end. sessionId={} ", id);
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
     * Returns the related Dispatcher
     *
     * @return related dispatcher
     */
    Dispatcher getDispatcher() {
        return dispatcher;
    }

    /**
     * Chesk whether the provided object is registered in the remote object
     * hashmap
     *
     * @param simonRemote
     * @return true, if the given object is registered, false if not
     */
    boolean isSimonRemoteRegistered(Object simonRemote) {
        if (simonRemote == null) {
            return false;
        }
        logger.trace("searching hash {} in {}", simonRemote.hashCode(), remoteobjectSet);
//        if (remoteobjectHashMap.containsKey(simonRemote.hashCode())) {
        if (remoteobjectSet.contains(simonRemote)) {
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
                Map<String, RemoteRef> refMap = sessionRefCount.get(sessionId);
                Collection<RemoteRef> values = refMap.values();
                for (RemoteRef remoteRef : values) {
                    i += remoteRef.getRefCount();
                }
            }
        }
        return i;
    }
}
