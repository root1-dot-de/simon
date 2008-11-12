package de.root1.simon;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.root1.simon.codec.messages.AbstractMessage;
import de.root1.simon.codec.messages.MsgEquals;
import de.root1.simon.codec.messages.MsgEqualsReturn;
import de.root1.simon.codec.messages.MsgHashCode;
import de.root1.simon.codec.messages.MsgHashCodeReturn;
import de.root1.simon.codec.messages.MsgInvoke;
import de.root1.simon.codec.messages.MsgInvokeReturn;
import de.root1.simon.codec.messages.MsgLookup;
import de.root1.simon.codec.messages.MsgLookupReturn;
import de.root1.simon.codec.messages.MsgToString;
import de.root1.simon.codec.messages.MsgToStringReturn;
import de.root1.simon.codec.messages.SimonMessageConstants;
import de.root1.simon.exceptions.LookupFailedException;
import de.root1.simon.utils.SimonClassLoader;

public class ProcessMessageRunnable implements Runnable {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private AbstractMessage abstractMessage;
	private IoSession session;
	private Dispatcher dispatcher;

	public ProcessMessageRunnable(Dispatcher dispatcher, IoSession session, AbstractMessage abstractMessage) {
		this.dispatcher = dispatcher;
		this.session = session;
		this.abstractMessage = abstractMessage;
	}

	public void run() {
		
		logger.debug("ProcessMessageRunnable: {}", abstractMessage);

		int msgType = abstractMessage.getMsgType();
		
		switch (msgType) {
		
			case SimonMessageConstants.MSG_LOOKUP:
				try {
					processLookup();
				} catch (LookupFailedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
				
			case SimonMessageConstants.MSG_LOOKUP_RETURN:
				processLookupReturn();
				break;
				
			case SimonMessageConstants.MSG_INVOKE:
				try {
					processInvoke();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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

			default:
				// FIXME what to do here ?!
				System.err.println("ProcessMessageRunnable: msgType="+msgType+" not supported!");
				System.exit(1);
				break;
		}
		
		
	}

	


	/**
	 * 
	 * processes a lookup
	 * @throws LookupFailedException 
	 */
	private void processLookup() throws LookupFailedException {
		logger.debug("begin");

		logger.debug("processing MsgLookup...");
		MsgLookup msg = (MsgLookup)abstractMessage;
		String remoteObjectName = msg.getRemoteObjectName();
		
		logger.debug("Sending result for remoteObjectName={}", remoteObjectName);
		
		MsgLookupReturn ret = new MsgLookupReturn();
		ret.setSequence(msg.getSequence());
		ret.setInterfaces(dispatcher.getLookupTable().getRemoteBinding(remoteObjectName).getClass().getInterfaces());
		session.write(ret);
		
		logger.debug("end");
	}
	
	private void processLookupReturn() {
		logger.debug("begin");
		
		logger.debug("processing MsgLookupReturn...");	
		MsgLookupReturn msg = (MsgLookupReturn)abstractMessage;
		Class<?>[] interfaces = msg.getInterfaces();
		
		logger.debug("Forward result to waiting monitor");
		
		logger.debug("remoteObjectName={}", interfaces);
		
		dispatcher.putResultToQueue(msg.getSequence(), msg);
		logger.debug("end");
	}
	
	private void processInvoke() throws ClassNotFoundException {
		logger.debug("begin");
		
		logger.debug("processing MsgInvoke...");
		MsgInvoke msg = (MsgInvoke) abstractMessage;
		Method method = msg.getMethod();
		Object[] arguments = msg.getArguments();
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
		
		
		
		String remoteObjectName = msg.getRemoteObjectName();
		
		logger.debug("ron={} method={} args={}", new Object[]{remoteObjectName, method, arguments});
		
		try {
			SimonRemote simonRemote = dispatcher.getLookupTable().getRemoteBinding(remoteObjectName);
			Object result = method.invoke(simonRemote, arguments);
			
			
			// register "SimonCallback"-results in lookup-table
			if (result instanceof SimonRemote){

				logger.debug("Result of method {} is instance of SimonRemote: {}", method, result);
				
				SimonRemoteInstance simonCallback = new SimonRemoteInstance(session,(SimonRemote)result);
				simonCallback.getId();
				
				dispatcher.getLookupTable().putRemoteInstanceBinding(session.getId(), simonCallback.getId(), (SimonRemote) result);
				result = simonCallback;
				
			}
			
			MsgInvokeReturn returnMsg = new MsgInvokeReturn();
			returnMsg.setSequence(msg.getSequence());
			
			returnMsg.setReturnValue(result);
			
			logger.debug("Sending result={}", returnMsg);
			
			session.write(returnMsg);
			
		} catch (LookupFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.debug("end");
	}

	private void processInvokeReturn() {
		logger.debug("begin");

		logger.debug("processing MsgInvokeReturn...");
		MsgInvokeReturn msg = (MsgInvokeReturn) abstractMessage;
		dispatcher.putResultToQueue(msg.getSequence(), msg);
		
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
		dispatcher.putResultToQueue(msg.getSequence(), msg);
		 
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
		dispatcher.putResultToQueue(msg.getSequence(), msg);
		 
		logger.debug("put result to queue={}", msg);
		 
		logger.debug("end");
	}

	private void processHashCode() {
		logger.debug("begin");
		
		logger.debug("processing MsgHashCode...");
		MsgHashCode msg = (MsgHashCode) abstractMessage;
		 
		String remoteObjectName = msg.getRemoteObjectName();
		int returnValue = -1;
		try {
			returnValue = dispatcher.getLookupTable().getRemoteBinding(remoteObjectName).hashCode();
		} catch (LookupFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		MsgHashCodeReturn returnMsg = new MsgHashCodeReturn();
		returnMsg.setSequence(msg.getSequence());
		returnMsg.setReturnValue(returnValue);
		session.write(returnMsg);
		logger.debug("end");
	}
	
	private void processHashCodeReturn() {
		logger.debug("begin");
		logger.debug("processing MsgHashCodeReturn...");
		 MsgHashCodeReturn msg = (MsgHashCodeReturn) abstractMessage;
		 dispatcher.putResultToQueue(msg.getSequence(), msg);
		 
		 logger.debug("put result to queue={}", msg);
		 
		 logger.debug("end");
	}

}
