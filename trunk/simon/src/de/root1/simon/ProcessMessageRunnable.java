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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

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
import de.root1.simon.codec.messages.MsgRawChannelData;
import de.root1.simon.codec.messages.MsgRawChannelDataReturn;
import de.root1.simon.codec.messages.MsgToString;
import de.root1.simon.codec.messages.MsgToStringReturn;
import de.root1.simon.codec.messages.SimonMessageConstants;
import de.root1.simon.exceptions.LookupFailedException;
import de.root1.simon.exceptions.SimonRemoteException;
import de.root1.simon.utils.SimonClassLoader;

/**
 * TODO document me
 * 
 * @author achr
 *
 */
public class ProcessMessageRunnable implements Runnable {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private AbstractMessage abstractMessage;
	private IoSession session;
	private Dispatcher dispatcher;

	protected ProcessMessageRunnable(Dispatcher dispatcher, IoSession session, AbstractMessage abstractMessage) {
		this.dispatcher = dispatcher;
		this.session = session;
		this.abstractMessage = abstractMessage;
	}

	public void run() {
		
		logger.debug("ProcessMessageRunnable: {}", abstractMessage);

		int msgType = abstractMessage.getMsgType();
		
		switch (msgType) {
		
			case SimonMessageConstants.MSG_LOOKUP:
				processLookup();
				break;
				
			case SimonMessageConstants.MSG_LOOKUP_RETURN:
				processLookupReturn();
				break;
				
			case SimonMessageConstants.MSG_INVOKE:
				processInvoke();
				break;
				
			case SimonMessageConstants.MSG_INVOKE_RETURN:
				processInvokeReturn();
				break;
				
			case SimonMessageConstants.MSG_TOSTRING:
				processToString();
				break;
				
			case SimonMessageConstants.MSG_TOSTRING_RETURN:
				processToStringReturn();
				break;

			case SimonMessageConstants.MSG_EQUALS:
				processEquals();
				break;

			case SimonMessageConstants.MSG_EQUALS_RETURN:
				processEqualsReturn();
				break;
				
			case SimonMessageConstants.MSG_HASHCODE:
				processHashCode();
				break;

			case SimonMessageConstants.MSG_HASHCODE_RETURN:
				processHashCodeReturn();
				break;
				
			case SimonMessageConstants.MSG_OPEN_RAW_CHANNEL:
				processOpenRawChannel();
				break;
				
			case SimonMessageConstants.MSG_OPEN_RAW_CHANNEL_RETURN:
				processOpenRawChannelReturn();
				break;
				
			case SimonMessageConstants.MSG_CLOSE_RAW_CHANNEL:
				processCloseRawChannel();
				break;
				
			case SimonMessageConstants.MSG_CLOSE_RAW_CHANNEL_RETURN:
				processCloseRawChannelReturn();
				break;
				
			case SimonMessageConstants.MSG_RAW_CHANNEL_DATA:
				processRawChannelData();
				break;
				
			case SimonMessageConstants.MSG_RAW_CHANNEL_DATA_RETURN:
				processRawChannelDataReturn();
				break;
				
			case SimonMessageConstants.MSG_PING:
				processPing();
				break;
				
			case SimonMessageConstants.MSG_PONG:
				processPong();
				break;

			default:
				// FIXME what to do here ?!
				logger.error("ProcessMessageRunnable: msgType={} not supported! terminating...",msgType);
				System.exit(1);
				break;
		}
		
	}

	private void processRawChannelDataReturn() {
		logger.debug("begin");

		logger.debug("processing MsgRawChannelDataReturn...");
		MsgRawChannelDataReturn msg = (MsgRawChannelDataReturn) abstractMessage;
		dispatcher.putResultToQueue(session, msg.getSequence(), msg);
		
		logger.debug("put result to queue={}", msg);
		
		logger.debug("end");
	}

	private void processPing() {
		logger.debug("begin");
		logger.debug("processing MsgPing...");

		logger.debug("replying pong");
		dispatcher.sendPong(session);
		
		logger.debug("end");
	}
	
	private void processPong() {
		logger.debug("begin");
		logger.debug("processing MsgPong...");

		dispatcher.getPingWatchdog().notifyPongReceived(session);
		logger.debug("end");
	}

	private void processOpenRawChannel() {
		logger.debug("begin");
		
		logger.debug("processing MsgOpenRawChannel...");
		MsgOpenRawChannel msg = (MsgOpenRawChannel) abstractMessage;
		 
		MsgOpenRawChannelReturn returnMsg = new MsgOpenRawChannelReturn();
		returnMsg.setSequence(msg.getSequence());
		returnMsg.setReturnValue(dispatcher.isRawChannelDataListenerRegistered(msg.getChannelToken()));
		session.write(returnMsg);	
		
		logger.debug("end");
	}

	private void processOpenRawChannelReturn() {
		logger.debug("begin");
		logger.debug("processing MsgOpenRawChannelReturn...");
		MsgOpenRawChannelReturn msg = (MsgOpenRawChannelReturn) abstractMessage;
		dispatcher.putResultToQueue(session, msg.getSequence(), msg);
		logger.debug("put result to queue={}", msg);
		logger.debug("end");
	}

	private void processCloseRawChannel() {
		logger.debug("begin");
		
		logger.debug("processing MsgCloseRawChannel...");
		MsgCloseRawChannel msg = (MsgCloseRawChannel) abstractMessage;
		 
		dispatcher.unprepareRawChannel(msg.getChannelToken());
		
		MsgCloseRawChannelReturn returnMsg = new MsgCloseRawChannelReturn();
		returnMsg.setSequence(msg.getSequence());
		returnMsg.setReturnValue(true);
		session.write(returnMsg);	
		
		logger.debug("end");
	}

	private void processCloseRawChannelReturn() {
		logger.debug("begin");
		logger.debug("processing MsgCloseRawChannelReturn...");
		MsgCloseRawChannelReturn msg = (MsgCloseRawChannelReturn) abstractMessage;
		dispatcher.putResultToQueue(session, msg.getSequence(), msg);
		logger.debug("put result to queue={}", msg);
		logger.debug("end");
	}

	private void processRawChannelData() {
		logger.debug("begin");
		
		logger.debug("processing MsgRawChannelData...");
		MsgRawChannelData msg = (MsgRawChannelData) abstractMessage;
		
		RawChannelDataListener rawChannelDataListener = dispatcher.getRawChannelDataListener(msg.getChannelToken());
		if (rawChannelDataListener!=null){
			logger.debug("writing data to {} for token {}.",rawChannelDataListener, msg.getChannelToken());
			rawChannelDataListener.write(msg.getData());
			logger.debug("data forwarded to listener for token {}", msg.getChannelToken());
			MsgRawChannelDataReturn returnMsg = new MsgRawChannelDataReturn();
			returnMsg.setSequence(msg.getSequence());
			session.write(returnMsg);
		} else {
			logger.error("trying to forward data to a not registered or already closed listener: token={} data={}",msg.getChannelToken(),msg.getData());
		}
				
		logger.debug("end");
	}

	/**
	 * 
	 * processes a lookup
	 * @throws LookupFailedException 
	 */
	private void processLookup() {
		logger.debug("begin");

		logger.debug("processing MsgLookup...");
		MsgLookup msg = (MsgLookup)abstractMessage;
		String remoteObjectName = msg.getRemoteObjectName();
		
		logger.debug("Sending result for remoteObjectName={}", remoteObjectName);
		
		MsgLookupReturn ret = new MsgLookupReturn();
		ret.setSequence(msg.getSequence());
		try {
			Class<?>[] interfaces = null;
			interfaces = dispatcher.getLookupTable().getRemoteBinding(remoteObjectName).getClass().getInterfaces();
			ret.setInterfaces(interfaces);
		} catch (LookupFailedException e) {
			logger.debug("Lookup for remote object '{}' failed: {}", remoteObjectName, e.getMessage());
			ret.setErrorMsg(e.getMessage());
		}
		session.write(ret);
		
		logger.debug("end");
	}
	
	private void processLookupReturn() {
		logger.debug("begin");
		
		logger.debug("processing MsgLookupReturn...");	
		MsgLookupReturn msg = (MsgLookupReturn)abstractMessage;
		
		logger.debug("Forward result to waiting monitor");
		dispatcher.putResultToQueue(session, msg.getSequence(), msg);
			
		logger.debug("end");
	}
	
	private void processInvoke() {
		logger.debug("begin");
		
		logger.debug("processing MsgInvoke...");
		
		Object result = null;
		
		MsgInvoke msg = (MsgInvoke) abstractMessage;
		
		// if received msg has an error
		if (msg.hasError()){
			result = new SimonRemoteException("Received MsgInvoke had errors. Cannot process invocation. error msg: "+msg.getErrorMsg());
		
			MsgInvokeReturn returnMsg = new MsgInvokeReturn();
			returnMsg.setSequence(msg.getSequence());
			
			returnMsg.setReturnValue(result);
			
			logger.debug("Sending result={}", returnMsg);
			
			session.write(returnMsg);
			logger.debug("end");
		}

		Method method = msg.getMethod();
		Object[] arguments = msg.getArguments();
		String remoteObjectName = msg.getRemoteObjectName();
		
		try {
			
			// ------------
			// replace existing SimonRemote objects with proxy object
			if (arguments != null) {
				
				for (int i = 0; i < arguments.length; i++) {
					
					// search the arguments for remote instances 
					if (arguments[i] instanceof SimonRemoteInstance) {
						
						final SimonRemoteInstance simonCallback = (SimonRemoteInstance) arguments[i];
						
						logger.debug("SimonCallback in args found. id={}", simonCallback.getId());					
						
						Class<?>[] listenerInterfaces = new Class<?>[1];
						listenerInterfaces[0] = Class.forName(simonCallback.getInterfaceName());
						
						// reimplant the proxy object
						arguments[i] = Proxy.newProxyInstance(SimonClassLoader.getClassLoader(this.getClass()), listenerInterfaces, new SimonProxy(dispatcher, session, simonCallback.getId()));
						logger.debug("proxy object for SimonCallback injected");
					} 
				} 
			} 
			// ------------
			
			logger.debug("ron={} method={} args={}", new Object[]{remoteObjectName, method, arguments});
			
			SimonRemote simonRemote;
			simonRemote = dispatcher.getLookupTable().getRemoteBinding(remoteObjectName);
			result = method.invoke(simonRemote, arguments);
			
			// register "SimonCallback"-results in lookup-table
			if (result instanceof SimonRemote){

				logger.debug("Result of method '{}' is instance of SimonRemote: {}", method, result);
				
				SimonRemoteInstance simonCallback = new SimonRemoteInstance(session,(SimonRemote)result);
				
				dispatcher.getLookupTable().putRemoteInstanceBinding(session.getId(), simonCallback.getId(), (SimonRemote) result);
				result = simonCallback;
				
			}
			
		} catch (LookupFailedException e) {
			result = new SimonRemoteException("Errow while invoking '"+remoteObjectName+"#"+method+"' due to exception: "+e.getMessage());
		} catch (IllegalArgumentException e) {
			result = e;
		} catch (IllegalAccessException e) {
			result = new SimonRemoteException("Errow while invoking '"+remoteObjectName+"#"+method+"' due to exception: "+e.getMessage());
		} catch (InvocationTargetException e) {
			result = e.getTargetException();
		} catch (Exception e) {
			result = new SimonRemoteException("Errow while invoking '"+remoteObjectName+"#"+method+"' due to exception: "+e.getMessage());
		}
		
		MsgInvokeReturn returnMsg = new MsgInvokeReturn();
		returnMsg.setSequence(msg.getSequence());
		
		returnMsg.setReturnValue(result);
		
		logger.debug("Sending result={}", returnMsg);
		
		session.write(returnMsg);
		logger.debug("end");
	}

	private void processInvokeReturn() {
		logger.debug("begin");

		logger.debug("processing MsgInvokeReturn...");
		MsgInvokeReturn msg = (MsgInvokeReturn) abstractMessage;
		dispatcher.putResultToQueue(session, msg.getSequence(), msg);
		
		logger.debug("put result to queue={}", msg);
		
		logger.debug("end");
	}

	
	private void processToString() {
		logger.debug("begin");
		
		logger.debug("processing MsgToString...");
		MsgToString msg = (MsgToString) abstractMessage;
		 
		String remoteObjectName = msg.getRemoteObjectName();
		String returnValue = null;
		try {
			returnValue = dispatcher.getLookupTable().getRemoteBinding(remoteObjectName).toString();
		} catch (LookupFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		MsgToStringReturn returnMsg = new MsgToStringReturn();
		returnMsg.setSequence(msg.getSequence());
		returnMsg.setReturnValue(returnValue);
		session.write(returnMsg);
		logger.debug("end");
	}

	private void processToStringReturn() {
		logger.debug("begin");
		logger.debug("processing MsgToStringReturn...");
		MsgToStringReturn msg = (MsgToStringReturn) abstractMessage;
		dispatcher.putResultToQueue(session, msg.getSequence(), msg);
		 
		logger.debug("put result to queue={}", msg);
		 
		logger.debug("end");
	}
	
	private void processEquals() {
		logger.debug("begin");
		
		logger.debug("processing MsgEquals...");
		MsgEquals msg = (MsgEquals) abstractMessage;
		 
		 String remoteObjectName = msg.getRemoteObjectName();
		 Object objectToCompareWith = msg.getObjectToCompareWith();
		 
		 boolean equalsResult = false;
		 try {
			 
			 // if the object is a remote object, get the object out of the lookuptable
			 if (objectToCompareWith instanceof SimonRemoteInstance) {
				 logger.debug("Given argument is SimonRemoteInstance");
				 
				 final String argumentRemoteObjectName = ((SimonRemoteInstance)objectToCompareWith).getRemoteObjectName();
				 
				 objectToCompareWith = dispatcher.getLookupTable().getRemoteBinding(argumentRemoteObjectName);
			 }
			 
			 SimonRemote remoteBinding = dispatcher.getLookupTable().getRemoteBinding(remoteObjectName);
			 equalsResult = remoteBinding.toString().equals(objectToCompareWith.toString());
			 
			 logger.debug("remoteBinding='{}' objectToCompareWith='{}' equalsResult={}", new Object[]{remoteBinding.toString(), objectToCompareWith.toString(), equalsResult});
			 
		} catch (LookupFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		MsgEqualsReturn returnMsg = new MsgEqualsReturn();
		returnMsg.setSequence(msg.getSequence());
		returnMsg.setEqualsResult(equalsResult);
		session.write(returnMsg);
		logger.debug("end");
	}

	private void processEqualsReturn() {
		logger.debug("begin");
		logger.debug("processing MsgEqualsReturn...");
		MsgEqualsReturn msg = (MsgEqualsReturn) abstractMessage;
		dispatcher.putResultToQueue(session, msg.getSequence(), msg);
		 
		logger.debug("put result to queue={}", msg);
		 
		logger.debug("end");
	}

	private void processHashCode() {
		logger.debug("begin");
		
		logger.debug("processing MsgHashCode...");
		MsgHashCode msg = (MsgHashCode) abstractMessage;
		 
		String remoteObjectName = msg.getRemoteObjectName();
		
		MsgHashCodeReturn returnMsg = new MsgHashCodeReturn();
		returnMsg.setSequence(msg.getSequence());

		int returnValue = -1;
		try {
			returnValue = dispatcher.getLookupTable().getRemoteBinding(remoteObjectName).hashCode();
		} catch (LookupFailedException e) {
			returnMsg.setErrorMsg("Failed looking up the remote object for getting the hash code. Error: "+e.getMessage());
		}
		
		returnMsg.setReturnValue(returnValue);
		session.write(returnMsg);
		logger.debug("end");
	}
	
	private void processHashCodeReturn() {
		logger.debug("begin");
		logger.debug("processing MsgHashCodeReturn...");
		 MsgHashCodeReturn msg = (MsgHashCodeReturn) abstractMessage;
		 dispatcher.putResultToQueue(session, msg.getSequence(), msg);
		 
		 logger.debug("put result to queue={}", msg);
		 
		 logger.debug("end");
	}

}
