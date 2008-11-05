package de.root1.simon;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.mina.core.session.IoSession;

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
	
	protected transient Logger _log = Logger.getLogger(this.getClass().getName());
	private AbstractMessage abstractMessage;
	private IoSession session;
	private Dispatcher dispatcher;

	public ProcessMessageRunnable(Dispatcher dispatcher, IoSession session, AbstractMessage abstractMessage) {
		this.dispatcher = dispatcher;
		this.session = session;
		this.abstractMessage = abstractMessage;
	}

	public void run() {
		
		if (_log.isLoggable(Level.FINER))
			_log.finer("ProcessMessageRunnable: "+abstractMessage);

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
		_log.fine("begin");

		_log.fine("processing MsgLookup...");
		MsgLookup msg = (MsgLookup)abstractMessage;
		String remoteObjectName = msg.getRemoteObjectName();
		
		if (_log.isLoggable(Level.FINE))
			_log.fine("Sending result for remoteObjectName="+remoteObjectName);
		
		MsgLookupReturn ret = new MsgLookupReturn();
		ret.setSequence(msg.getSequence());
		ret.setInterfaces(dispatcher.getLookupTable().getRemoteBinding(remoteObjectName).getClass().getInterfaces());
		session.write(ret);
		
		_log.fine("end");
	}
	
	private void processLookupReturn() {
		_log.fine("begin");
		
		_log.fine("processing MsgLookupReturn...");	
		MsgLookupReturn msg = (MsgLookupReturn)abstractMessage;
		Class<?>[] interfaces = msg.getInterfaces();
		
		_log.fine("Forward result to waiting monitor");
		
		if (_log.isLoggable(Level.FINE))
			_log.fine("remoteObjectName="+interfaces);
		
		dispatcher.putResultToQueue(msg.getSequence(), msg);
		_log.fine("end");
	}
	
	private void processInvoke() throws ClassNotFoundException {
		_log.fine("begin");
		
		_log.fine("processing MsgInvoke...");
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
					
					if (_log.isLoggable(Level.FINER))
						_log.finer("SimonCallback in args found. id="+simonCallback.getId());					
					
					Class<?>[] listenerInterfaces = new Class<?>[1];
					listenerInterfaces[0] = Class.forName(simonCallback.getInterfaceName());
					
					// reimplant the proxy object
					arguments[i] = Proxy.newProxyInstance(SimonClassLoader.getClassLoader(this.getClass()), listenerInterfaces, new SimonProxy(dispatcher, session, simonCallback.getId()));
					_log.finer("proxy object for SimonCallback injected");
				} 
			} 
		} 
		// ------------
		
		
		
		String remoteObjectName = msg.getRemoteObjectName();
		
		if (_log.isLoggable(Level.FINE))
			_log.fine("ron="+remoteObjectName+" method="+method+" args="+arguments);
		
		try {
			SimonRemote simonRemote = dispatcher.getLookupTable().getRemoteBinding(remoteObjectName);
			Object result = method.invoke(simonRemote, arguments);
			
			
			// register "SimonCallback"-results in lookup-table
			if (result instanceof SimonRemote){

				if (_log.isLoggable(Level.FINER))
					_log.finer("Result of method "+method+" is instance of SimonRemote: "+result);
				
				SimonRemoteInstance simonCallback = new SimonRemoteInstance(session,(SimonRemote)result);
				simonCallback.getId();
				
				dispatcher.getLookupTable().putRemoteInstanceBinding(session.getId(), simonCallback.getId(), (SimonRemote) result);
				result = simonCallback;
				
			}
			
			MsgInvokeReturn returnMsg = new MsgInvokeReturn();
			returnMsg.setSequence(msg.getSequence());
			
			returnMsg.setReturnValue(result);
			
			if (_log.isLoggable(Level.FINE))
				_log.fine("Sending result="+returnMsg);
			
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
		_log.fine("end");
	}

	private void processInvokeReturn() {
		_log.fine("begin");

		_log.fine("processing MsgInvokeReturn...");
		MsgInvokeReturn msg = (MsgInvokeReturn) abstractMessage;
		dispatcher.putResultToQueue(msg.getSequence(), msg);
		
		if (_log.isLoggable(Level.FINE))
			_log.fine("put result to queue="+msg);
		
		_log.fine("end");
	}

	
	private void processToString() {
		_log.fine("begin");
		
		_log.fine("processing MsgToString...");
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
		_log.fine("end");
	}

	private void processToStringReturn() {
		_log.fine("begin");
		_log.fine("processing MsgToStringReturn...");
		 MsgToStringReturn msg = (MsgToStringReturn) abstractMessage;
		 dispatcher.putResultToQueue(msg.getSequence(), msg);
		 
		 if (_log.isLoggable(Level.FINE))
			 _log.fine("put result to queue="+msg);
		 
		_log.fine("end");
	}
	
	private void processEquals() {
		_log.fine("begin");
		
		_log.fine("processing MsgEquals...");
		MsgEquals msg = (MsgEquals) abstractMessage;
		 
		 String remoteObjectName = msg.getRemoteObjectName();
		 Object objectToCompareWith = msg.getObjectToCompareWith();
		 
		 boolean equalsResult = false;
		 try {
			 
			 // if the object is a remote object, get the object out of the lookuptable
			 if (objectToCompareWith instanceof SimonRemoteInstance) {
				 _log.finer("Given argument is SimonRemoteInstance");
				 
				 final String argumentRemoteObjectName = ((SimonRemoteInstance)objectToCompareWith).getRemoteObjectName();
				 
				 objectToCompareWith = dispatcher.getLookupTable().getRemoteBinding(argumentRemoteObjectName);
			 }
			 
			 SimonRemote remoteBinding = dispatcher.getLookupTable().getRemoteBinding(remoteObjectName);
			 equalsResult = remoteBinding.toString().equals(objectToCompareWith.toString());
			 
			 if (_log.isLoggable(Level.FINER))
				 _log.finer("remoteBinding='"+remoteBinding.toString()+"' objectToCompareWith='"+objectToCompareWith.toString()+"' equalsResult="+equalsResult);
			 
		} catch (LookupFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		MsgEqualsReturn returnMsg = new MsgEqualsReturn();
		returnMsg.setSequence(msg.getSequence());
		returnMsg.setEqualsResult(equalsResult);
		session.write(returnMsg);
		_log.fine("end");
	}

	private void processEqualsReturn() {
		_log.fine("begin");
		_log.fine("processing MsgEqualsReturn...");
		 MsgEqualsReturn msg = (MsgEqualsReturn) abstractMessage;
		 dispatcher.putResultToQueue(msg.getSequence(), msg);
		 
		 if (_log.isLoggable(Level.FINE))
			 _log.fine("put result to queue="+msg);
		 
		_log.fine("end");
	}

	private void processHashCode() {
		_log.fine("begin");
		
		_log.fine("processing MsgHashCode...");
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
		_log.fine("end");
	}
	
	private void processHashCodeReturn() {
		_log.fine("begin");
		_log.fine("processing MsgHashCodeReturn...");
		 MsgHashCodeReturn msg = (MsgHashCodeReturn) abstractMessage;
		 dispatcher.putResultToQueue(msg.getSequence(), msg);
		 
		 if (_log.isLoggable(Level.FINE))
			 _log.fine("put result to queue="+msg);
		 
		_log.fine("end");
	}

}
