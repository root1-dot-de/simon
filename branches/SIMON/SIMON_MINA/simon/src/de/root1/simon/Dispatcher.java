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
import java.util.Map;
import java.util.concurrent.ExecutorService;

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
import de.root1.simon.codec.messages.MsgInvoke;
import de.root1.simon.codec.messages.MsgInvokeReturn;
import de.root1.simon.codec.messages.MsgLookup;
import de.root1.simon.codec.messages.MsgLookupReturn;
import de.root1.simon.codec.messages.MsgOpenRawChannel;
import de.root1.simon.codec.messages.MsgOpenRawChannelReturn;
import de.root1.simon.codec.messages.MsgPing;
import de.root1.simon.codec.messages.MsgRawChannelData;
import de.root1.simon.codec.messages.MsgToString;
import de.root1.simon.codec.messages.MsgToStringReturn;
import de.root1.simon.exceptions.LookupFailedException;
import de.root1.simon.exceptions.SessionException;
import de.root1.simon.exceptions.SimonException;
import de.root1.simon.exceptions.SimonRemoteException;

/**
 * TODO documentation
 * 
 * @author ACHR
 */
public class Dispatcher implements IoHandler{
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	/** The table that holds all the registered/bind remote objects */
	private LookupTable lookupTable;
	
	/** a simple counter that is used for creating sequence IDs */
	private int sequenceIdCounter = 0;
	
	/**
	 * The map that holds the relation between the request ID and the received
	 * result. If a request is placed, the map contains the sequenceID and the
	 * corresponding monitor object. If the result is present, the monitor object
	 * is replaced with the result
	 */
	private Map<Integer, Object> requestMonitorAndReturnMap = Collections.synchronizedMap(new HashMap<Integer, Object>());
	
	/** a memory map for the client the unwrap the incoming return value after executing a method on the server */
	private Map<Integer, Class<?>> requestReturnType = Collections.synchronizedMap(new HashMap<Integer, Class<?>>());
	
	/** the thread-pool where the worker-threads live in */
	private ExecutorService messageProcessorPool = null;

	/** Shutdown flag. If set to true, the dispatcher is going to shutdown itself and all related stuff */
	private boolean shutdownInProgress;

	/** indicates if the dispatcher is running or not */
	private boolean isRunning;

	/** an identifier string to determine to which server this dispatcher is connected to  */
	private final String serverString;

	/** TODO document me */
	private HashMap<Integer, RawChannelDataListener> rawChannelMap = new HashMap<Integer, RawChannelDataListener>();

	/** TODO document me */
	private ArrayList<Integer> tokenList = new ArrayList<Integer>();
	
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
		
//		// FIXME ...
//		if (serverString==null) {
//			sequenceIdCounter = 1000;
//		}
		
		logger.debug("end");
	}
	
	/**
	 * 
	 * Sends a remote object lookup to the server
	 * 
	 * @param remoteObjectName
	 * @return the object we made the lookup for
	 * @throws SimonRemoteException 
	 */
	protected MsgLookupReturn invokeLookup(IoSession session, String remoteObjectName) throws LookupFailedException, SimonRemoteException {
		checkForInvalidState("Simon.lookup({...}, "+remoteObjectName+")");
		final int sequenceId = generateSequenceId(); 
		
		logger.debug("begin sequenceId={} session={}", sequenceId, session);
		

 		// create a monitor that waits for the request-result
		final Monitor monitor = createMonitor(sequenceId);
		
		MsgLookup msgLookup = new MsgLookup();
		msgLookup.setSequence(sequenceId);
		msgLookup.setRemoteObjectName(remoteObjectName);
		
		session.write(msgLookup);
		
		logger.debug("data send. waiting for answer for sequenceId={}",sequenceId);

		waitForResult(monitor);
		MsgLookupReturn result = (MsgLookupReturn) getRequestResult(sequenceId);			
		
		logger.debug("got answer for sequenceId={}",sequenceId);
		logger.trace("end sequenceId={}",sequenceId);
		
		return result;

	}

	
	
	/**
	 * sends a requested invocation to the server
	 * 
	 * @param remoteObjectName
	 * @param method
	 * @param args
	 * @return the method's result
	 * @throws SimonRemoteException if there's a problem with the communication
	 */	 
 	protected Object invokeMethod(IoSession session, String remoteObjectName, Method method, Object[] args) throws SimonRemoteException {
 		
 		checkForInvalidState(method.toString());
 		
 		final int sequenceId = generateSequenceId(); 
		
		logger.debug("begin sequenceId={} session={}", sequenceId, session);
		

 		// create a monitor that waits for the request-result
		final Monitor monitor = createMonitor(sequenceId);
		
		// register remote instance objects in the lookup-table
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				if (args[i] instanceof SimonRemote) {
					SimonRemoteInstance sc = new SimonRemoteInstance(session,(SimonRemote)args[i]);
					
					logger.debug("SimonRemoteInstance found! id={}",sc.getId());
					
					lookupTable.putRemoteInstanceBinding(session.getId(), sc.getId(), (SimonRemote) args[i]);
					
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
		
		logger.debug("data send. waiting for answer for sequenceId={}",sequenceId);

		waitForResult(monitor);
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
	 * @param key the key associated with the network connection
	 * @param remoteObjectName the remote object on which the call has to be made
	 * @return the result of the remote "toString()" call.
	 */
	protected String invokeToString(IoSession session, String remoteObjectName) {
		checkForInvalidState("toString()");

		final int sequenceId = generateSequenceId(); 
		
		logger.debug("begin sequenceId={} session={}", sequenceId, session);

 		// create a monitor that waits for the request-result
		final Monitor monitor = createMonitor(sequenceId);
		
		MsgToString msgInvoke = new MsgToString();
		msgInvoke.setSequence(sequenceId);
		msgInvoke.setRemoteObjectName(remoteObjectName);
		
		session.write(msgInvoke);
		
		logger.debug("data send. waiting for answer for sequenceId={}", sequenceId);

		waitForResult(monitor);
		MsgToStringReturn result = (MsgToStringReturn) getRequestResult(sequenceId);		
		
		if (result.hasError())
			throw new SimonRemoteException(result.getErrorMsg());
		
		logger.debug("got answer for sequenceId={}", sequenceId);
		logger.debug("end sequenceId={}", sequenceId);
		
		return result.getReturnValue();
	}

	
	/**
	 * 
	 * @TODO document me ...
	 * 
	 * @param remoteObjectName the 
	 * @return
	 */
	protected int invokeHashCode(IoSession session, String remoteObjectName) throws SimonRemoteException {
		
		checkForInvalidState("hashCode()");

		final int sequenceId = generateSequenceId(); 
		
		logger.debug("begin sequenceId={} session={}", sequenceId, session);

 		// create a monitor that waits for the request-result
		final Monitor monitor = createMonitor(sequenceId);
		
		MsgHashCode msgInvoke = new MsgHashCode();
		msgInvoke.setSequence(sequenceId);
		msgInvoke.setRemoteObjectName(remoteObjectName);
		
		session.write(msgInvoke);
		
		logger.debug("data send. waiting for answer for sequenceId={}", sequenceId);

		waitForResult(monitor);
		MsgHashCodeReturn result = (MsgHashCodeReturn) getRequestResult(sequenceId);			
			
		if (result.hasError())
			throw new SimonRemoteException(result.getErrorMsg());
		
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
	 */
	protected boolean invokeEquals(IoSession session, String remoteObjectName, Object objectToCompareWith) {
		checkForInvalidState("equals()");

		final int sequenceId = generateSequenceId(); 
		
		logger.debug("begin sequenceId={} session={}", sequenceId, session);

 		// create a monitor that waits for the request-result
		final Monitor monitor = createMonitor(sequenceId);
		
		MsgEquals msgEquals = new MsgEquals();
		msgEquals.setSequence(sequenceId);
		msgEquals.setRemoteObjectName(remoteObjectName);
		msgEquals.setObjectToCompareWith(objectToCompareWith);
		
		session.write(msgEquals);
		
		logger.debug("data send. waiting for answer for sequenceId={}", sequenceId);

		waitForResult(monitor);
		MsgEqualsReturn result = (MsgEqualsReturn) getRequestResult(sequenceId);		
		
		if (result.hasError())
			throw new SimonRemoteException(result.getErrorMsg());
			
		logger.debug("got answer for sequenceId={}", +sequenceId);
		logger.debug("end sequenceId={}", sequenceId);
		
		return result.getEqualsResult();
	}

	/**
	 * Waits until the result for a request described by the monitor is present
	 * @param monitor the monitor related to the request
	 */
	private void waitForResult(final Monitor monitor) {

		int sequenceId = monitor.getSequenceId();
		// wait for result
		synchronized (monitor) {
			try {
				while(!isRequestResultPresent(sequenceId)) {
					monitor.wait(Statics.MONITOR_WAIT_TIMEOUT);
					logger.trace("still waiting for result for sequenceId={}",sequenceId);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}


	/**
	 * Wake the process with the related sequenceId
	 * 
	 * @param sequenceId the process to wake  
	 */
	protected void wakeWaitingProcess(int sequenceId) {
		
		logger.debug("begin. wakeing sequenceId={}", sequenceId);
		
		// FIXME how to wake and present error?
		
		logger.trace("end. wakeing sequenceId={}", sequenceId);
	}

	/**
	 * This method is called from worker-threads which processed an invocation and have data 
	 * ready that has to be returned to the "caller".
	 * after adding the result to the queue, the waiting request-method is waked.
	 * 
	 * @param sequenceId the sequence id that is waiting for the result
	 * @param msg the result itself
	 */
	public void putResultToQueue(int sequenceId, AbstractMessage msg){
		logger.debug("begin");
		
		logger.debug("sequenceId={} msg={}", sequenceId, msg);
		
//		synchronized (requestMonitorAndReturnMap) {
			Object monitor = requestMonitorAndReturnMap.get(sequenceId);
			requestMonitorAndReturnMap.put(sequenceId, msg);
			synchronized (monitor) {
				monitor.notify();
			}
//		}
		logger.debug("end");
	}
	
	
	/**
	 * for internal use only
	 */
	protected LookupTable getLookupTable() {
		return lookupTable;
	}

	
	
	/**
	 * 
	 * Removes the return type from the list of awaited result types for a specific request ID.
	 * 
	 * @param sequenceId the request id which was waiting for a result of the type saved in the list
	 * @return the return type which has been removed
	 */
	protected Class<?> removeRequestReturnType(int sequenceId) {
		
		synchronized (requestReturnType) {
			return requestReturnType.remove(sequenceId);
		}
		
	}

	/**
	 * 
	 * All received results are saved in a queue. With this method you can get the received result 
	 * by its sequenceId.
	 * <br/>
	 * <b>Attention:</b> Be sure that you only call this method if you were notified by the receiver! 
	 * 
	 * @param sequenceId the sequenceId which is related to the result
	 * @return the received result
	 */
	protected Object getResult(int sequenceId){
		synchronized (requestMonitorAndReturnMap) {
			return getRequestResult(sequenceId);			
		}
	}


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
	 * Returns whether the dispatcher is still in run() or not
	 * @return boolean
	 */
	protected boolean isRunning(){
		return isRunning;
	}
	
	/**
	 * TODO document me
	 * @param method
	 */
	private void checkForInvalidState(String method) {
		if (shutdownInProgress) throw new SessionException("Cannot handle method call \""+method+"\" while shutdown.");
		if (!isRunning) throw new SessionException("Cannot handle method call \""+method+"\" on already closed session.");
	}
	
	/**
	 * 
	 * create a monitor that waits for the request-result that 
	 * is associated with the given request-id
	 * 
	 * @param sequenceId
	 * @return the monitor used for waiting for the result
	 */
	private Monitor createMonitor(final int sequenceId) {
		logger.debug("begin");
		
		final Monitor monitor = new Monitor(sequenceId);

		synchronized (requestMonitorAndReturnMap) {
			requestMonitorAndReturnMap.put(sequenceId, monitor);
		}
		
		logger.debug("created monitor for sequenceId={}", sequenceId);
		
		logger.debug("end");
		return monitor;
	}
	
	/**
	 * 
	 * Checks if the request result is an {@link SimonRemoteException}. 
	 * If yes, the exception is thrown, if not, the result is returned
	 * 
	 * @param sequenceId the sequence-id related to the result
	 * @return the result of the request. May be null if there is no result yet.
	 */
	private Object getRequestResult(final int sequenceId) {
		logger.debug("getting result for sequenceId={}", sequenceId);
		
		Object o = requestMonitorAndReturnMap.remove(sequenceId); 
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
	private boolean isRequestResultPresent(final int sequenceId){
		boolean present = false;
		// if the contained object is NOT an instance of Monitor, present=true
		if (!(requestMonitorAndReturnMap.get(sequenceId) instanceof Monitor)) 
			present = true;
		logger.debug("Result for sequenceId={} present: {}",sequenceId, present);
		return present;
	}
	
	/**
	 * 
	 * Generates a request ID<br>
	 * IDs have a unique value from 0..Integer.MAX_VALUE<br>
	 * The range should be big enough so that there should 
	 * not be two oder more identical IDs
	 * 
	 * @return a request ID
	 */
	private synchronized Integer generateSequenceId() {
		return (++sequenceIdCounter == Integer.MAX_VALUE ? 0 : sequenceIdCounter);
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.mina.core.service.IoHandler#exceptionCaught(org.apache.mina.core.session.IoSession, java.lang.Throwable)
	 */
	public void exceptionCaught(IoSession session, Throwable throwable)
			throws Exception {
		if (logger.isTraceEnabled()){
			logger.trace("exception Caught. session={} cause={}", session, throwable);
			StackTraceElement[] stackTrace = throwable.getStackTrace();
			for (StackTraceElement stackTraceElement : stackTrace) {
				System.err.println(stackTraceElement);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.mina.core.service.IoHandler#messageReceived(org.apache.mina.core.session.IoSession, java.lang.Object)
	 */
	public void messageReceived(IoSession session, Object message) throws Exception {
		logger.debug("Received message from {}", session.getRemoteAddress());
		
		AbstractMessage abstractMessage = (AbstractMessage) message;
		
		logger.debug("Put message into message processor pool");
		messageProcessorPool.execute(new ProcessMessageRunnable(this, session, abstractMessage));
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.mina.core.service.IoHandler#messageSent(org.apache.mina.core.session.IoSession, java.lang.Object)
	 */
	public void messageSent(IoSession session, Object msg) throws Exception {
		logger.debug("message sent. session={} msg={}", session, msg);
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.mina.core.service.IoHandler#sessionClosed(org.apache.mina.core.session.IoSession)
	 */
	public void sessionClosed(IoSession session) throws Exception {
		logger.debug("################################################");
		logger.debug("######## session closed. session={}",session);
		lookupTable.unreference(session.getId());
		// remove attached references
		logger.debug("######## Removing session attributes ...");
		
		logger.debug("########  -> {}",Statics.SESSION_ATTRIBUTE_DISPATCHER);
		session.removeAttribute(Statics.SESSION_ATTRIBUTE_DISPATCHER);
		
		logger.debug("########  -> {}",Statics.SESSION_ATTRIBUTE_LOOKUPTABLE);
		session.removeAttribute(Statics.SESSION_ATTRIBUTE_LOOKUPTABLE);
		
		logger.debug("################################################");
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
		logger.debug("session idle. session={} idleStatus={}", session, idleStatus);
		if (idleStatus == IdleStatus.WRITER_IDLE || idleStatus == IdleStatus.BOTH_IDLE) {
			logger.trace("sending ping to test session");
			sendPing(session);
		}
	}

	private void sendPing(IoSession session) {
		checkForInvalidState("ping()");

		final int sequenceId = generateSequenceId(); 
		
		logger.debug("begin sequenceId={} session={}", sequenceId, session);
		
		MsgPing msgInvoke = new MsgPing();
		msgInvoke.setSequence(sequenceId);
		
		session.write(msgInvoke);
		
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
	 * TODO document me
	 * @return
	 */
	protected RawChannel openRawChannel(IoSession session, int channelToken) {
		checkForInvalidState("openRawChannel()");

		final int sequenceId = generateSequenceId(); 
		
		logger.debug("begin sequenceId={} session={} token={}", new Object[]{sequenceId, session, channelToken});
		

 		// create a monitor that waits for the request-result
		final Monitor monitor = createMonitor(sequenceId);
		
		MsgOpenRawChannel msgOpenRawChannel = new MsgOpenRawChannel();
		msgOpenRawChannel.setSequence(sequenceId);
		msgOpenRawChannel.setChannelToken(channelToken);
		
		session.write(msgOpenRawChannel);
		
		logger.debug("data send. waiting for answer for sequenceId={}", sequenceId);

		waitForResult(monitor);
		MsgOpenRawChannelReturn result = (MsgOpenRawChannelReturn) getRequestResult(sequenceId);			
		
		logger.debug("got answer for sequenceId={}", sequenceId);
		logger.debug("end sequenceId={}", sequenceId);

		if (result.getReturnValue()==true){
			logger.debug("Creating RawChannel object with token={}", channelToken);
			return new RawChannel(this, session, channelToken);
		}
		
		throw new SimonRemoteException("channel could not be opened. Maybe token was wrong?!");
	}

	/** TODO document me */
	protected int prepareRawChannel(RawChannelDataListener listener) {
		int channelToken = getRawChannelToken();
		synchronized (rawChannelMap) {
			rawChannelMap.put(channelToken, listener);	
		}
		logger.trace("rawChannelMap={}",rawChannelMap);
		return channelToken;
	}
	
	/** TODO document me */
	protected boolean isRawChannelDataListenerRegistered(int channelToken){
		logger.trace("searching in map for token={} map={}",channelToken,rawChannelMap);
		synchronized (rawChannelMap) {
			return rawChannelMap.containsKey(channelToken);	
		}
	}
	
	/** TODO document me */
	protected RawChannelDataListener getRawChannelDataListener(int channelToken){
		logger.trace("getting listener token={} map={}",channelToken,rawChannelMap);
		synchronized (rawChannelMap) {
			return rawChannelMap.get(channelToken);	
		}
	}

	/** TODO document me */
	private int getRawChannelToken() {
		synchronized (tokenList) {
			
			for (int i=Integer.MIN_VALUE; i<Integer.MAX_VALUE; i++){
				if (!tokenList.contains(i)){
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
	private void releaseToken(int channelToken){
		synchronized (tokenList) {
			tokenList.remove((Object)channelToken);
		}
	}
	
	/**
	 * TODO document me
	 * @param channelToken
	 */
	protected void unprepareRawChannel(int channelToken){
		logger.debug("token={}",channelToken);
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
	 */
	protected void writeRawData(IoSession session, int channelToken, ByteBuffer byteBuffer){
		checkForInvalidState("writeRawData()");

		final int sequenceId = generateSequenceId(); 
		
		logger.debug("begin sequenceId={} session={}", sequenceId, session);
		
		MsgRawChannelData msgRawChannelData = new MsgRawChannelData();
		msgRawChannelData.setSequence(sequenceId);
		msgRawChannelData.setChannelToken(channelToken);
		msgRawChannelData.setData(byteBuffer);
		
		session.write(msgRawChannelData);
		
		logger.debug("end. data send for sequenceId={} and channelToken={}", sequenceId, channelToken);
	}

	protected void closeRawChannel(IoSession session, int channelToken) {
		checkForInvalidState("closeRawChannel()");

		final int sequenceId = generateSequenceId(); 
		
		logger.debug("begin sequenceId={} session={} token={}", new Object[]{sequenceId, session, channelToken});
		

 		// create a monitor that waits for the request-result
		final Monitor monitor = createMonitor(sequenceId);
		
		MsgCloseRawChannel msgCloseRawChannel = new MsgCloseRawChannel();
		msgCloseRawChannel.setSequence(sequenceId);
		msgCloseRawChannel.setChannelToken(channelToken);
		
		session.write(msgCloseRawChannel);
		
		logger.debug("data send. waiting for answer for sequenceId={}", sequenceId);

		waitForResult(monitor);
		MsgCloseRawChannelReturn result  = (MsgCloseRawChannelReturn) getRequestResult(sequenceId);			
		
		logger.debug("got answer for sequenceId={}", sequenceId);
		logger.debug("end sequenceId={}", sequenceId);

		if (result.getReturnValue()==true){
			return;
		}
		
		throw new SimonRemoteException("channel could not be opened. Maybe token was wrong?!");
	}
	
}
