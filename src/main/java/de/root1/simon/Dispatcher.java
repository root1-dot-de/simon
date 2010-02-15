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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.root1.simon.codec.messages.AbstractMessage;
import de.root1.simon.codec.messages.MsgCloseRawChannel;
import de.root1.simon.codec.messages.MsgCloseRawChannelReturn;
import de.root1.simon.codec.messages.MsgEquals;
import de.root1.simon.codec.messages.MsgEqualsReturn;
import de.root1.simon.codec.messages.MsgHashCode;
import de.root1.simon.codec.messages.MsgHashCodeReturn;
import de.root1.simon.codec.messages.MsgInterfaceLookup;
import de.root1.simon.codec.messages.MsgInterfaceLookupReturn;
import de.root1.simon.codec.messages.MsgInvoke;
import de.root1.simon.codec.messages.MsgInvokeReturn;
import de.root1.simon.codec.messages.MsgNameLookup;
import de.root1.simon.codec.messages.MsgNameLookupReturn;
import de.root1.simon.codec.messages.MsgOpenRawChannel;
import de.root1.simon.codec.messages.MsgOpenRawChannelReturn;
import de.root1.simon.codec.messages.MsgPing;
import de.root1.simon.codec.messages.MsgPong;
import de.root1.simon.codec.messages.MsgRawChannelData;
import de.root1.simon.codec.messages.MsgToString;
import de.root1.simon.codec.messages.MsgToStringReturn;
import de.root1.simon.exceptions.LookupFailedException;
import de.root1.simon.exceptions.SessionException;
import de.root1.simon.exceptions.SimonException;
import de.root1.simon.exceptions.SimonRemoteException;
import de.root1.simon.utils.Utils;

/**
 * TODO documentation
 * 
 * @author ACHR
 */
public class Dispatcher implements IoHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    /** The table that holds all the registered/bind remote objects */
    private final LookupTable lookupTable;
    /** a simple counter that is used for creating sequence IDs */
    private final AtomicInteger sequenceIdCounter = new AtomicInteger(0);
    /**
     * The map that holds the relation between the sequenceID and the received
     * result. If a request is placed, the map contains the sequenceID and the
     * corresponding monitor object. If the result is present, the monitor object
     * is replaced with the result
     */
    private final Map<Integer, Object> requestMonitorAndResultMap = Collections.synchronizedMap(new HashMap<Integer, Object>());
    /**
     * This map contains pairs of sessions and list of open requests
     * This is needed to do a clean shutdown of a session
     */
    private final Map<IoSession, List<Integer>> sessionHasRequestPlaced = Collections.synchronizedMap(new HashMap<IoSession, List<Integer>>());
//	/** a memory map for the client the unwrap the incoming return value after executing a method on the server */
//	private Map<Integer, Class<?>> requestReturnType = Collections.synchronizedMap(new HashMap<Integer, Class<?>>());
    /** the thread-pool where the worker-threads live in */
    private ExecutorService messageProcessorPool = null;
    /** Shutdown flag. If set to true, the dispatcher is going to shutdown itself and all related stuff */
    private boolean shutdownInProgress;
    /** indicates if the dispatcher is running or not */
    private boolean isRunning;
    /** an identifier string to determine to which server this dispatcher is connected to  */
    private final String serverString;
    /** TODO document me */
    private final HashMap<Integer, RawChannelDataListener> rawChannelMap = new HashMap<Integer, RawChannelDataListener>();
    /** TODO document me */
    private final ArrayList<Integer> tokenList = new ArrayList<Integer>();
    /** TODO document me */
    private final PingWatchdog pingWatchdog;
    /** TODO document me */
    private int writeTimeout = Statics.DEFAULT_WRITE_TIMEOUT;
    /**
     * A map containing remote object names and a related list with closed listeners
     */
    private final Map<String, List<ClosedListener>> remoteObjectClosedListenersList = Collections.synchronizedMap(new HashMap<String, List<ClosedListener>>());

    /**
     * TODO document me
     * @return the pingTimeOut
     */
    protected int getWriteTimeout() {
        return writeTimeout;
    }

    /**
     * TODO document me
     * @param pingTimeOut the pingTimeOut to set
     */
    protected void setPingTimeOut(int pingTimeOut) {
        this.writeTimeout = pingTimeOut;
    }

    /**
     * TODO document me
     * @param remoteObjectName
     * @return
     */
    protected List<ClosedListener> removeClosedListenerList(String remoteObjectName) {
        return remoteObjectClosedListenersList.remove(remoteObjectName);
    }

    /**
     * TODO document me
     * @param listener
     * @param remoteObjectName
     */
    protected void addClosedListener(ClosedListener listener, String remoteObjectName) {
        if (!remoteObjectClosedListenersList.containsKey(remoteObjectName)) {
            List<ClosedListener> closedListeners = Collections.synchronizedList(new ArrayList<ClosedListener>());
            closedListeners.add(listener);
            remoteObjectClosedListenersList.put(remoteObjectName, closedListeners);
        } else {
            remoteObjectClosedListenersList.get(remoteObjectName).add(listener);
        }
    }

    /**
     * TODO document me
     * @param listener
     * @param remoteObjectName
     */
    protected boolean removeClosedListener(ClosedListener listener, String remoteObjectName) {
        if (remoteObjectClosedListenersList.containsKey(remoteObjectName)) {

            // remove the listener from the list
            boolean result = remoteObjectClosedListenersList.get(remoteObjectName).remove(listener);

            // if the list is now empty, remove the liste from the map
            if (remoteObjectClosedListenersList.get(remoteObjectName).size() == 0) {
                remoteObjectClosedListenersList.remove(remoteObjectName);
            }
            // return the result of the removal
            return result;

        } else {
            return false;
        }
    }

    /**
     *
     * Creates a packet dispatcher which delegates
     * the packet-reading to {@link ProcessMessageRunnable}'s which run in the given <code>threadPool</code>
     *
     * @param serverString an identifier string to determine to which server this dispatcher is
     * connected to. this must be set to <code>null</code> if this dispatcher is a server dispatcher.
     * @param threadPool the pool where the {@link ProcessMessageRunnable}'s run in
     */
    public Dispatcher(String serverString, ExecutorService threadPool) {
        logger.debug("begin");

        isRunning = true;

        this.serverString = serverString;
        this.lookupTable = new LookupTable(this);

        this.messageProcessorPool = threadPool;

        this.pingWatchdog = new PingWatchdog(this);

        logger.debug("end");
    }

    /**
     *
     * Sends a remote object lookup to the server
     *
     * @param session the related session over which the invoke request comes
     * @param remoteObjectName the remote object to lookup
     * @return the name lookup return message
     * @throws SimonRemoteException
     */
    protected MsgNameLookupReturn invokeNameLookup(IoSession session, String remoteObjectName) throws LookupFailedException, SimonRemoteException {
        checkForInvalidState(session, "Simon.lookup({...}, " + remoteObjectName + ")");
        final int sequenceId = generateSequenceId();

        logger.debug("begin sequenceId={} session={}", sequenceId, session);


        // create a monitor that waits for the request-result
        final Monitor monitor = createMonitor(session, sequenceId);

        MsgNameLookup msgNameLookup = new MsgNameLookup();
        msgNameLookup.setSequence(sequenceId);
        msgNameLookup.setRemoteObjectName(remoteObjectName);

        session.write(msgNameLookup);

        logger.debug("data send. waiting for answer for sequenceId={}", sequenceId);

        waitForResult(session, monitor);
        MsgNameLookupReturn result = (MsgNameLookupReturn) getRequestResult(sequenceId);

        logger.debug("got answer for sequenceId={}", sequenceId);
        logger.trace("end sequenceId={}", sequenceId);

        return result;

    }

    /**
     *
     * Sends a remote object lookup to the server
     *
     * @param session the related session over which the invoke request comes
     * @param canonicalInterfaceName the canonical name of the interface
     * @return the interface lookup return message
     * @throws SimonRemoteException
     */
    protected MsgInterfaceLookupReturn invokeInterfaceLookup(IoSession session, String canonicalInterfaceName) throws LookupFailedException, SimonRemoteException {
        checkForInvalidState(session, "Simon.lookup({...}, " + canonicalInterfaceName + ")");
        final int sequenceId = generateSequenceId();

        logger.debug("begin sequenceId={} session={}", sequenceId, session);

        // create a monitor that waits for the request-result
        final Monitor monitor = createMonitor(session, sequenceId);

        MsgInterfaceLookup msgInterfaceLookup = new MsgInterfaceLookup();
        msgInterfaceLookup.setSequence(sequenceId);
        msgInterfaceLookup.setCanonicalInterfaceName(canonicalInterfaceName);

        session.write(msgInterfaceLookup);

        logger.debug("data send. waiting for answer for sequenceId={}", sequenceId);

        waitForResult(session, monitor);
        MsgInterfaceLookupReturn result = (MsgInterfaceLookupReturn) getRequestResult(sequenceId);

        logger.debug("got answer for sequenceId={}", sequenceId);
        logger.trace("end sequenceId={}", sequenceId);

        return result;

    }

    /*

     * Sends a method invocation request to the remote host.
     *
     * @param session the related session over which the invoke request comes
     * @param remoteObjectName the remote object
     * @param method the method ti invoke on the remote
     * @param args the arguments for the method
     * @return the result of the invoked method
     * @throws SimonRemoteException
     */
    protected Object invokeMethod(IoSession session, String remoteObjectName, Method method, Object[] args) throws SimonRemoteException {

        checkForInvalidState(session, method.toString());

        final int sequenceId = generateSequenceId();

        logger.debug("begin sequenceId={} session={}", sequenceId, session);


        // create a monitor that waits for the request-result
        final Monitor monitor = createMonitor(session, sequenceId);

        // register remote instance objects in the lookup-table
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                if (Utils.isValidRemote(args[i])) {
                    SimonRemoteInstance sc = new SimonRemoteInstance(session, args[i]);

                    logger.debug("SimonRemoteInstance found! id={}", sc.getId());

                    lookupTable.putRemoteInstanceBinding(session.getId(), sc.getId(), args[i]);

                    args[i] = sc; // overwrite arg with wrapped remote instance-interface
                }
            }
        }

        MsgInvoke msgInvoke = new MsgInvoke();
        msgInvoke.setSequence(sequenceId);
        msgInvoke.setRemoteObjectName(remoteObjectName);
        msgInvoke.setMethod(method);
        msgInvoke.setArguments(args);

        session.write(msgInvoke);

        logger.debug("data send. waiting for answer for sequenceId={}", sequenceId);

        waitForResult(session, monitor);
        MsgInvokeReturn result = (MsgInvokeReturn) getRequestResult(sequenceId);
        logger.debug("got answer for sequenceId={}", sequenceId);

//		if (result.hasError()) {
//			logger.debug("An error occured. Returning SimonRemoteException. Error: {}",result.getErrorMsg());
//			logger.debug("end sequenceId={}", sequenceId);
//			return new SimonRemoteException(result.getErrorMsg());
//		}

        logger.debug("end sequenceId={}", sequenceId);
        return result.getReturnValue();

    }

    /**
     *
     * Sends a "toString()" request to the remote host.
     *
     * @param session the related session over which the invoke request comes
     * @param remoteObjectName the remote object
     * @return the result of the remote "toString()" call
     * @throws SimonRemoteException
     */
    protected String invokeToString(IoSession session, String remoteObjectName) throws SimonRemoteException {
        checkForInvalidState(session, "toString()");

        final int sequenceId = generateSequenceId();

        logger.debug("begin sequenceId={} session={}", sequenceId, session);

        // create a monitor that waits for the request-result
        final Monitor monitor = createMonitor(session, sequenceId);

        MsgToString msgInvoke = new MsgToString();
        msgInvoke.setSequence(sequenceId);
        msgInvoke.setRemoteObjectName(remoteObjectName);

        session.write(msgInvoke);

        logger.debug("data send. waiting for answer for sequenceId={}", sequenceId);

        waitForResult(session, monitor);
        MsgToStringReturn result = (MsgToStringReturn) getRequestResult(sequenceId);

        if (result.hasError()) {
            throw new SimonRemoteException(result.getErrorMsg());
        }

        logger.debug("got answer for sequenceId={}", sequenceId);
        logger.debug("end sequenceId={}", sequenceId);

        return result.getReturnValue();
    }

    /**
     *
     * Invokes the hashCode() method on the remote object
     *
     * @param session the related session over which the invoke request comes
     * @param remoteObjectName the remote object
     * @return the result of the remote "hashCode()" call
     * @throws SimonRemoteException
     */
    protected int invokeHashCode(IoSession session, String remoteObjectName) throws SimonRemoteException {

        checkForInvalidState(session, "hashCode()");

        final int sequenceId = generateSequenceId();

        logger.debug("begin sequenceId={} session={}", sequenceId, session);

        // create a monitor that waits for the request-result
        final Monitor monitor = createMonitor(session, sequenceId);

        MsgHashCode msgInvoke = new MsgHashCode();
        msgInvoke.setSequence(sequenceId);
        msgInvoke.setRemoteObjectName(remoteObjectName);

        session.write(msgInvoke);

        logger.debug("data send. waiting for answer for sequenceId={}", sequenceId);

        waitForResult(session, monitor);
        MsgHashCodeReturn result = (MsgHashCodeReturn) getRequestResult(sequenceId);

        if (result.hasError()) {
            throw new SimonRemoteException(result.getErrorMsg());
        }

        logger.debug("got answer for sequenceId={}", sequenceId);
        logger.debug("end sequenceId={}", sequenceId);

        return result.getReturnValue();
    }

    /**
     *
     * Forwards an "equals()" call to the remote side to be handled there
     *
     * @param key the key to which the invocation is sent
     * @param remoteObjectName the name of the remote object that has to be compared
     * @param objectToCompareWith the object to which the remote object is compared with
     * @return the result of the comparison
     * @throws SimonRemoteException
     */
    protected boolean invokeEquals(IoSession session, String remoteObjectName, Object objectToCompareWith) throws SimonRemoteException {
        checkForInvalidState(session, "equals()");

        final int sequenceId = generateSequenceId();

        logger.debug("begin sequenceId={} session={}", sequenceId, session);

        // create a monitor that waits for the request-result
        final Monitor monitor = createMonitor(session, sequenceId);

        MsgEquals msgEquals = new MsgEquals();
        msgEquals.setSequence(sequenceId);
        msgEquals.setRemoteObjectName(remoteObjectName);
        msgEquals.setObjectToCompareWith(objectToCompareWith);

        session.write(msgEquals);

        logger.debug("data send. waiting for answer for sequenceId={}", sequenceId);

        waitForResult(session, monitor);
        MsgEqualsReturn result = (MsgEqualsReturn) getRequestResult(sequenceId);

        if (result.hasError()) {
            throw new SimonRemoteException(result.getErrorMsg());
        }

        logger.debug("got answer for sequenceId={}", +sequenceId);
        logger.debug("end sequenceId={}", sequenceId);

        return result.getEqualsResult();
    }

    /**
     * Waits at most one hour for the result of request described by the monitor.
     * If result is not present within this time, a SimonRemoteException will be placed as the result.
     * This is to ensure that the client cannot wait forever for a result.
     *
     * @param session the session on which the request was placed
     * @param monitor the monitor related to the request
     */
    private void waitForResult(IoSession session, final Monitor monitor) {

        int sequenceId = monitor.getSequenceId();
        int counter = 0;

        // wait for result
        synchronized (monitor) {
            try {
                long startWaiting = System.currentTimeMillis();
                while (!isRequestResultPresent(sequenceId)) {

                    // just make sure that the while-loop cannot wait forever for the result
                    // 60min: 60min * 60sec * 1000ms = 3600000ms
                    // 3600000ms / 200ms = 18000 loops with 200ms wait time
                    if (counter++ == 18000) {
                        putResultToQueue(session, sequenceId, new SimonRemoteException("Waited too long for invocation result."));
                    }

                    monitor.wait(Statics.MONITOR_WAIT_TIMEOUT);

                    logger.trace("still waiting for result for sequenceId={}. Waiting since {}ms.", sequenceId, (System.currentTimeMillis()-startWaiting));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method is called from worker-threads which processed an invocation and have data
     * ready that has to be returned to the "caller".
     * after adding the result to the map (means: replacing the monitor with the result),
     * the waiting request-method is waked.
     *
     * @param sequenceId the sequence id that is waiting for the result
     * @param o the result itself
     */
    public void putResultToQueue(final IoSession session, final int sequenceId, final Object o) {
        logger.debug("begin");

        logger.debug("sequenceId={} msg={}", sequenceId, o);

        // remove the sequenceId from the map/list of open requests
        synchronized (sessionHasRequestPlaced) {

            assert sessionHasRequestPlaced.containsKey(session);

            // check if there is already a list with requests for this session
            List<Integer> requestListForSession = sessionHasRequestPlaced.get(session);

            assert requestListForSession != null;

            synchronized (requestListForSession) {
                assert requestListForSession.contains(sequenceId);
                requestListForSession.remove((Integer) sequenceId);
                // if there is no more request open, remove the session from the list
                if (requestListForSession.isEmpty()) {
                    sessionHasRequestPlaced.remove(session);
                }
            }


        }

        // retrieve monitor
        Object monitor = requestMonitorAndResultMap.get(sequenceId);
        // replace monitor with result
        requestMonitorAndResultMap.put(sequenceId, o);

        // notify monitor
        synchronized (monitor) {
            monitor.notify();
        }
        logger.debug("end");
    }

    /**
     * for internal use only
     */
    protected LookupTable getLookupTable() {
        return lookupTable;
    }

//	/**
//	 * 
//	 * Removes the return type from the list of awaited result types for a specific request ID.
//	 * 
//	 * @param sequenceId the request id which was waiting for a result of the type saved in the list
//	 * @return the return type which has been removed
//	 */
//	protected Class<?> removeRequestReturnType(int sequenceId) {
//		
//		synchronized (requestReturnType) {
//			return requestReturnType.remove(sequenceId);
//		}
//		
//	}
//	/**
//	 * 
//	 * All received results are saved in a queue. With this method you can get the received result 
//	 * by its sequenceId.
//	 * <br/>
//	 * <b>Attention:</b> Be sure that you only call this method if you were notified by the receiver! 
//	 * 
//	 * @param sequenceId the sequenceId which is related to the result
//	 * @return the received result
//	 */
//	protected Object getResult(int sequenceId){
//		synchronized (requestMonitorAndResultMap) {
//			return getRequestResult(sequenceId);			
//		}
//	}
    /**
     *
     * Initiates a shutdown at the dispatcher and all related things
     *
     */
    public void shutdown() {
        logger.debug("begin");

        shutdownInProgress = true;
        messageProcessorPool.shutdown();

        while (!messageProcessorPool.isShutdown()) {
            logger.debug("waiting for messageProcessorPool to shutdown...");
            try {
                Thread.sleep(Statics.WAIT_FOR_SHUTDOWN_SLEEPTIME);
            } catch (InterruptedException e) {
                // nothing to do
            }
        }
        lookupTable.cleanup();
        isRunning = false;
        logger.debug("shutdown completed");
        logger.debug("end");
    }

    /**
     *
     * Returns the identifier string which determines to which server this dispatcher is connected to
     *
     * @return the identifier string. this is <code>null</code> if this dispatcher is a server dispatcher
     */
    public String getServerString() {
        return serverString;
    }

    /**
     * Returns whether this is an server dispatcher or not
     * @return true if server dispatcher, false if not
     */
    protected boolean isServerDispatcher() {
        return (serverString == null ? true : false);
    }

    /**
     * Returns whether the dispatcher is still in run() or not
     * @return boolean
     */
    protected boolean isRunning() {
        return isRunning;
    }

    /**
     * TODO document me
     * @param method
     * @throws SessionException
     */
    private void checkForInvalidState(IoSession session, String method) throws SessionException {
        if (shutdownInProgress) {
            throw new SessionException("Cannot handle method call \"" + method + "\" while shutdown.");
        }
        if (!isRunning || session.isClosing()) {
            throw new SessionException("Cannot handle method call \"" + method + "\" on already closed session.");
        }
    }

    /**
     *
     * create a monitor that waits for the request-result that
     * is associated with the given request-id
     *
     * @param sequenceId
     * @return the monitor used for waiting for the result
     */
    private Monitor createMonitor(final IoSession session, final int sequenceId) {
        logger.debug("begin");

        final Monitor monitor = new Monitor(sequenceId);

        synchronized (sessionHasRequestPlaced) {
            // check if there is already a list with requests for this session
            if (!sessionHasRequestPlaced.containsKey(session)) {
                List<Integer> requestListForSession = new ArrayList<Integer>();
                requestListForSession.add(sequenceId);
                sessionHasRequestPlaced.put(session, requestListForSession);
            } else {
                // .. otherwise, get the list and add the sequenceId
                sessionHasRequestPlaced.get(session).add(sequenceId);
            }

        }

        // put the monitor in the result-map
        requestMonitorAndResultMap.put(sequenceId, monitor);

        logger.debug("created monitor for sequenceId={}", sequenceId);

        logger.debug("end");
        return monitor;
    }

    /**
     * Returns the result of the already placed request.
     * Make sure that you where notified about the received result.
     * Checks if the request result is an {@link SimonRemoteException}.
     * If yes, the exception is thrown, if not, the result is returned
     *
     * @param sequenceId the sequence-id related to the result
     * @return the result of the request. May be null if there is no result yet.
     * @throws SimonRemoteException
     */
    private Object getRequestResult(final int sequenceId) throws SimonRemoteException {
        logger.debug("getting result for sequenceId={}", sequenceId);

        Object o = requestMonitorAndResultMap.remove(sequenceId);

        if (o instanceof SimonRemoteException) {
            logger.debug("result is an exception, throwing it ...");
            throw ((SimonRemoteException) o);
        }
        return o;
    }

    /**
     * Returns whether the given sequenceId already has a result present or not
     * @param sequenceId the sequence-id related to the result
     * @return true, if the result is present, false if not
     */
    private boolean isRequestResultPresent(final int sequenceId) {
        boolean present = false;
        // if the contained object is NOT an instance of Monitor, present=true
        if (!(requestMonitorAndResultMap.get(sequenceId) instanceof Monitor)) {
            present = true;
        }
        logger.debug("Result for sequenceId={} present: {}", sequenceId, present);
        return present;
    }

    /**
     *
     * Generates a sequence ID<br>
     * IDs have a unique value from 0..Integer.MAX_VALUE<br>
     * The range should be big enough so that there should
     * not be two oder more identical IDs
     *
     * @return a request ID
     */
    private synchronized Integer generateSequenceId() {
        // if maximum reached, get maximum and set back to zero. otherwise just return the incremented value
        return (sequenceIdCounter.incrementAndGet() == Integer.MAX_VALUE ? sequenceIdCounter.getAndSet(0) : sequenceIdCounter.intValue());
    }

    /**
     * Interrupts all waiting requests for given session.
     * Returned value would be a SimonRemoteException
     * @param session the session which requests to be interrupted
     */
    private void interruptWaitingRequests(IoSession session) {
        List<Integer> remove = sessionHasRequestPlaced.get(session);
        // if there is a list with objects to remove
        if (remove != null) {
            List<Integer> removeCopy = new ArrayList<Integer>(remove);
            for (Integer sequenceId : removeCopy) {
                putResultToQueue(session, sequenceId, new SimonRemoteException("session was closed."));
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.apache.mina.core.service.IoHandler#exceptionCaught(org.apache.mina.core.session.IoSession, java.lang.Throwable)
     */
    public void exceptionCaught(IoSession session, Throwable throwable)
            throws Exception {
        if (logger.isTraceEnabled()) {

            StringBuffer sb = new StringBuffer();


            StackTraceElement[] stackTrace = throwable.getStackTrace();
            for (StackTraceElement stackTraceElement : stackTrace) {
               sb.append(stackTraceElement.toString());
               sb.append("\n");
            }

            logger.trace("exception Caught. session={} cause={} stack=\n{}", new Object[]{Utils.longToHexString(session.getId()), throwable, sb.toString()});
            
        }
        logger.debug("Closing the session now! session={}", Utils.longToHexString(session.getId()));
        session.close(true);
    }

    /*
     * (non-Javadoc)
     * @see org.apache.mina.core.service.IoHandler#messageReceived(org.apache.mina.core.session.IoSession, java.lang.Object)
     */
    public void messageReceived(IoSession session, Object message) throws Exception {
        logger.debug("Received message from session {}", Utils.longToHexString(session.getId()));
        AbstractMessage abstractMessage = (AbstractMessage) message;
        messageProcessorPool.execute(new ProcessMessageRunnable(this, session, abstractMessage));
    }

    /*
     * (non-Javadoc)
     * @see org.apache.mina.core.service.IoHandler#messageSent(org.apache.mina.core.session.IoSession, java.lang.Object)
     */
    public void messageSent(IoSession session, Object msg) throws Exception {
        logger.debug("Message sent to session session={} msg='{}'", Utils.longToHexString(session.getId()), msg);
    }

    /*
     * (non-Javadoc)
     * @see org.apache.mina.core.service.IoHandler#sessionClosed(org.apache.mina.core.session.IoSession)
     */
    public void sessionClosed(IoSession session) throws Exception {
        String id = Utils.longToHexString(session.getId());
        
        logger.debug("{} ################################################", id);
        logger.debug("{} ######## session closed", id);

        lookupTable.unreference(session.getId());
        interruptWaitingRequests(session);

        // remove attached references
        logger.debug("{} ######## Removing session attributes ...", id);

        logger.debug("{} ########  -> {}", id, Statics.SESSION_ATTRIBUTE_DISPATCHER);
        session.removeAttribute(Statics.SESSION_ATTRIBUTE_DISPATCHER);

        logger.debug("{} ########  -> {}", id, Statics.SESSION_ATTRIBUTE_LOOKUPTABLE);
        session.removeAttribute(Statics.SESSION_ATTRIBUTE_LOOKUPTABLE);

        // notify all still attached closed listeners that the one and only session to the server is closed.
        Iterator<String> iterator = remoteObjectClosedListenersList.keySet().iterator();
        while (iterator.hasNext()) {
            String ron = iterator.next();
            List<ClosedListener> list = remoteObjectClosedListenersList.remove(ron);
            for (ClosedListener closedListener : list) {
                closedListener.closed();
            }
            list.clear();
        }

        // FIXME possible fix for issue #57
        AbstractLookup.releaseDispatcher(this);

        logger.debug("{} ################################################", id);
    }

    /*
     * (non-Javadoc)
     * @see org.apache.mina.core.service.IoHandler#sessionCreated(org.apache.mina.core.session.IoSession)
     */
    public void sessionCreated(IoSession session) throws Exception {
        logger.debug("session created. session={}", session);
        session.setAttribute(Statics.SESSION_ATTRIBUTE_LOOKUPTABLE, lookupTable); // attach the lookup table to the session
        session.setAttribute(Statics.SESSION_ATTRIBUTE_DISPATCHER, this); // attach a reference to the dispatcher.

    }

    /*
     * (non-Javadoc)
     * @see org.apache.mina.core.service.IoHandler#sessionIdle(org.apache.mina.core.session.IoSession, org.apache.mina.core.session.IdleStatus)
     */
    public void sessionIdle(IoSession session, IdleStatus idleStatus) throws Exception {
        logger.debug("session idle. session={} idleStatus={}", Utils.longToHexString(session.getId()), idleStatus);

//		if (isServerDispatcher())

        if (!session.isClosing()) {
            if (idleStatus == IdleStatus.READER_IDLE || idleStatus == IdleStatus.BOTH_IDLE) {
                logger.trace("sending ping to test session {}", Utils.longToHexString(session.getId()));
                sendPing(session);
            }
        }
    }

    private void sendPing(IoSession session) throws SessionException {
        checkForInvalidState(session, "ping()");
        pingWatchdog.waitForPong(session);

        final int sequenceId = generateSequenceId();

        logger.debug("begin sequenceId={} session={}", sequenceId, session);

        MsgPing msgPing = new MsgPing();
        msgPing.setSequence(sequenceId);

        session.write(msgPing);

        logger.debug("end. data send.");
    }

    public void sendPong(IoSession session) throws SessionException {
        checkForInvalidState(session, "pong()");

        final int sequenceId = generateSequenceId();

        logger.debug("begin sequenceId={} session={}", sequenceId, session);

        MsgPong msgPong = new MsgPong();
        msgPong.setSequence(sequenceId);

        session.write(msgPong);

        logger.debug("end. data send.");
    }

    /*
     * (non-Javadoc)
     * @see org.apache.mina.core.service.IoHandler#sessionOpened(org.apache.mina.core.session.IoSession)
     */
    public void sessionOpened(IoSession session) throws Exception {
        logger.debug("session opened. session={}", session);
    }

    /**
     * Opens the a raw channel on the given session with the specified token
     * @return the opened RawChannel
     * @throws SimonRemoteException if a problem occured while opening the raw channel
     */
    protected RawChannel openRawChannel(IoSession session, int channelToken) throws SimonRemoteException {
        checkForInvalidState(session, "openRawChannel()");

        final int sequenceId = generateSequenceId();

        logger.debug("begin sequenceId={} session={} token={}", new Object[]{sequenceId, session, channelToken});


        // create a monitor that waits for the request-result
        final Monitor monitor = createMonitor(session, sequenceId);

        MsgOpenRawChannel msgOpenRawChannel = new MsgOpenRawChannel();
        msgOpenRawChannel.setSequence(sequenceId);
        msgOpenRawChannel.setChannelToken(channelToken);

        session.write(msgOpenRawChannel);

        logger.debug("data send. waiting for answer for sequenceId={}", sequenceId);

        waitForResult(session, monitor);
        MsgOpenRawChannelReturn result = (MsgOpenRawChannelReturn) getRequestResult(sequenceId);

        logger.debug("got answer for sequenceId={}", sequenceId);
        logger.debug("end sequenceId={}", sequenceId);

        if (result.getReturnValue() == true) {
            logger.debug("Creating RawChannel object with token={}", channelToken);
            return new RawChannel(this, session, channelToken);
        }

        throw new SimonRemoteException("channel could not be opened. Maybe token was wrong?!");
    }

    /** TODO document me
     * @throws SimonException */
    protected int prepareRawChannel(RawChannelDataListener listener) throws SimonException {
        int channelToken = getRawChannelToken();
        synchronized (rawChannelMap) {
            rawChannelMap.put(channelToken, listener);
        }
        logger.trace("rawChannelMap={}", rawChannelMap);
        return channelToken;
    }

    /** TODO document me */
    protected boolean isRawChannelDataListenerRegistered(int channelToken) {
        logger.trace("searching in map for token={} map={}", channelToken, rawChannelMap);
        synchronized (rawChannelMap) {
            return rawChannelMap.containsKey(channelToken);
        }
    }

    /** TODO document me */
    protected RawChannelDataListener getRawChannelDataListener(int channelToken) {
        logger.trace("getting listener token={} map={}", channelToken, rawChannelMap);
        synchronized (rawChannelMap) {
            return rawChannelMap.get(channelToken);
        }
    }

    /** TODO document me
     * @throws SimonException */
    private int getRawChannelToken() throws SimonException {
        synchronized (tokenList) {

            for (int i = Integer.MIN_VALUE; i < Integer.MAX_VALUE; i++) {
                if (!tokenList.contains(i)) {
                    tokenList.add(i);
                    return i;
                }
            }
        }
        throw new SimonException("no more token available");
    }

    /**
     * TODO document me
     */
    private void releaseToken(int channelToken) {
        synchronized (tokenList) {
            // if not cast to integer object, the entry at
            // index "channelToken" is removed,
            // instead of the channelToken itself.
            // means: wrong remove() method would be used
            tokenList.remove((Integer) channelToken);
        }
    }

    /**
     * TODO document me
     * @param channelToken
     */
    protected void unprepareRawChannel(int channelToken) {
        logger.debug("token={}", channelToken);
        releaseToken(channelToken);
        synchronized (rawChannelMap) {
            RawChannelDataListener rawChannelDataListener = rawChannelMap.remove(channelToken);
            rawChannelDataListener.close();
        }
    }

    /**
     * TODO document me
     * @param session
     * @param channelToken
     * @param byteBuffer
     * @throws SimonRemoteException
     */
    protected void writeRawData(IoSession session, int channelToken, ByteBuffer byteBuffer) throws SimonRemoteException {
        checkForInvalidState(session, "writeRawData()");

        final int sequenceId = generateSequenceId();

        logger.debug("begin sequenceId={} session={}", sequenceId, session);

        // create a monitor that waits for the request-result
        final Monitor monitor = createMonitor(session, sequenceId);

        MsgRawChannelData msgRawChannelData = new MsgRawChannelData();
        msgRawChannelData.setSequence(sequenceId);
        msgRawChannelData.setChannelToken(channelToken);
        msgRawChannelData.setData(byteBuffer);

        session.write(msgRawChannelData);

        logger.debug("data send. waiting for answer for sequenceId={}", sequenceId);

        waitForResult(session, monitor);
//		MsgRawChannelDataReturn result = (MsgRawChannelDataReturn) getRequestResult(sequenceId);			
        getRequestResult(sequenceId); //retrieve the return msg to remove the monitor etc.

        logger.debug("end. got ack for data send for sequenceId={} and channelToken={}", sequenceId, channelToken);
    }

    protected void closeRawChannel(IoSession session, int channelToken) throws SimonRemoteException {
        checkForInvalidState(session, "closeRawChannel()");

        final int sequenceId = generateSequenceId();

        logger.debug("begin sequenceId={} session={} token={}", new Object[]{sequenceId, session, channelToken});


        // create a monitor that waits for the request-result
        final Monitor monitor = createMonitor(session, sequenceId);

        MsgCloseRawChannel msgCloseRawChannel = new MsgCloseRawChannel();
        msgCloseRawChannel.setSequence(sequenceId);
        msgCloseRawChannel.setChannelToken(channelToken);

        session.write(msgCloseRawChannel);

        logger.debug("data send. waiting for answer for sequenceId={}", sequenceId);

        waitForResult(session, monitor);
        MsgCloseRawChannelReturn result = (MsgCloseRawChannelReturn) getRequestResult(sequenceId);

        logger.debug("got answer for sequenceId={}", sequenceId);
        logger.debug("end sequenceId={}", sequenceId);

        if (result.getReturnValue() == true) {
            return;
        }

        throw new SimonRemoteException("channel could not be opened. Maybe token was wrong?!");
    }

    /**
     * Returns the PingWatchdog that checks the session connectivity
     * @return the current instance of PingWatchdog
     */
    protected PingWatchdog getPingWatchdog() {
        return pingWatchdog;
    }
}
