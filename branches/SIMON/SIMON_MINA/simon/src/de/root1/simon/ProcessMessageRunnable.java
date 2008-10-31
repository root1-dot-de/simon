package de.root1.simon;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.logging.Logger;

import org.apache.mina.core.session.IoSession;

import de.root1.simon.codec.messages.AbstractMessage;
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
				break;

			case SimonMessageConstants.MSG_EQUALS_RETURN:
				break;
				
			case SimonMessageConstants.MSG_HASHCODE:
				processHashCode();
				break;

			case SimonMessageConstants.MSG_HASHCODE_RETURN:
				processHashCodeReturn();
				break;

			default:
				System.err.println("ProcessMessageRunnable:: msgType="+msgType+" not supported!");
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
		_log.fine("ron="+remoteObjectName+" method="+method+" args="+arguments);
		
		try {
			SimonRemote simonRemote = dispatcher.getLookupTable().getRemoteBinding(remoteObjectName);
			_log.finest("arguments="+arguments);
			Object result = method.invoke(simonRemote, arguments);
			
			
			// register "SimonCallback"-results in lookup-table
			if (result instanceof SimonRemote){
				_log.finer("Result of method "+method+" is instance of SimonRemote: "+result);
				
				SimonRemoteInstance simonCallback = new SimonRemoteInstance(session,(SimonRemote)result);
				simonCallback.getId();
				
//				dispatcher.getLookupTable().putRemoteBinding(simonCallback.getId(), (SimonRemote)result);
				dispatcher.getLookupTable().putRemoteInstanceBinding(session.getId(), simonCallback.getId(), (SimonRemote) result);
				result = simonCallback;
				
			}
			
			
			MsgInvokeReturn returnMsg = new MsgInvokeReturn();
			returnMsg.setSequence(msg.getSequence());
			
			returnMsg.setReturnValue(result);
			
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
		_log.fine("put result to queue="+msg);
		_log.fine("end");
	}


	//	/**
//	 * 
//	 * processes a request for a "equals()" call on a remote-object
//	 * 
//	 * @param remoteObjectName
//	 * @param object
//	 * @throws IOException
//	 * @throws LookupFailedException 
//	 */
//	private void processEquals(String remoteObjectName, Object object) throws IOException, LookupFailedException{
//		
//		_log.fine("begin");
//		
//		if (object instanceof SimonRemoteInstance) {
//			_log.finer("given argument is SimonRemoteInstance");
//			
//			final String argumentRemoteObjectName = ((SimonRemoteInstance)object).getRemoteObjectName();
//
//			object = dispatcher.getLookupTable().getRemoteBinding(argumentRemoteObjectName);
//		}
//		
//		if (_log.isLoggable(Level.FINER))
//			_log.finer("argument="+object);
//		
//		SimonRemote remoteBinding = dispatcher.getLookupTable().getRemoteBinding(remoteObjectName);
//		
//		if (_log.isLoggable(Level.FINER))
//			_log.finer("remoteBinding="+remoteBinding);
//		
////		final boolean equals = remoteBinding.equals(object);	
//		final boolean equals = remoteBinding.toString().equals(object.toString());
//
//		if (_log.isLoggable(Level.FINER))
//			_log.finer("result="+equals);
//		
//		
//		TxPacket packet = new TxPacket();
//		packet.setHeader(Statics.EQUALS_RETURN_PACKET, requestID);
//		packet.put(equals ? (byte) 1 : (byte) 0);
//		packet.setComplete();
//		
//		dispatcher.send(key,packet.getByteBuffer());
//		
//		_log.fine("end");
//	}
//	
	

	//	/**
//	 * 
//	 * TODO Documentation to be done
//	 * @param remoteObjectName
//	 * @throws IOException
//	 * @throws ClassNotFoundException
//	 * @throws LookupFailedException 
//	 */
//	private void processInvokeMethod(String remoteObjectName) throws NotSerializableException, IOException, ClassNotFoundException, LookupFailedException{
//		
//		if (_log.isLoggable(Level.FINE))
//			_log.fine("begin. requestID="+requestID);
//		
//		dispatcher.incIncomingInvocationCounter();
//		
//		remoteObjectName = Utils.getString(packetBody);
//		final long methodHash = packetBody.getLong();
//		
//		final Method method = dispatcher.getLookupTable().getMethod(remoteObjectName, methodHash);
//		final Class<?>[] parameterTypes = method.getParameterTypes();			
//		final Object[] args = new Object[parameterTypes.length];
//		
//		if (_log.isLoggable(Level.FINER)){
//			_log.finer("calling method name="+method.getName());
//		}
//		
//		// unwrapping the arguments
//		for (int i = 0; i < args.length; i++) {
//			args[i]=Utils.unwrapValue(parameterTypes[i], packetBody);							
//		}
//		
//		Object result = null;
//					
//		// replace existing SimonRemote objects with proxy object
//		if (args != null) {
//
//			for (int i = 0; i < args.length; i++) {
//				
//				// search the arguments for remote instances 
//				if (args[i] instanceof SimonRemoteInstance) {
//					
//					final SimonRemoteInstance simonCallback = (SimonRemoteInstance) args[i];
//					_log.finer("SimonCallback in args found. id="+simonCallback.getId());					
//										
//					Class<?>[] listenerInterfaces = new Class<?>[1];
//					listenerInterfaces[0] = Class.forName(simonCallback.getInterfaceName());
//
//					// reimplant the proxy object
//					args[i] = Proxy.newProxyInstance(SimonClassLoader.getClassLoader(this.getClass()), listenerInterfaces, new SimonProxy(dispatcher, key, simonCallback.getId()));
//					_log.finer("proxy object for SimonCallback injected");
//				} 
//			} 
//		} 
//		
//		try {
//
//			if (_log.isLoggable(Level.FINER))
//				_log.finer("start invoking method='"+method+"'. requestID="+requestID);
//			
//			result = method.invoke(dispatcher.getLookupTable().getRemoteBinding(remoteObjectName), args);
//
//			if (_log.isLoggable(Level.FINER))
//				_log.finer("end invoking method='"+method+"'. requestID="+requestID+" result="+result);
//
//			
//			// register "SimonCallback"-results in lookup-table
//			if (result instanceof SimonRemote){
//				_log.finer("Result of method is instance of SimonRemote");
//				
//				SimonRemoteInstance simonCallback = new SimonRemoteInstance(key,(SimonRemote)result);
//				simonCallback.getId();
//
////				dispatcher.getLookupTable().putRemoteBinding(simonCallback.getId(), (SimonRemote)result);
//				dispatcher.getLookupTable().putRemoteInstanceBinding(key, simonCallback.getId(), (SimonRemote) result);
//				result = simonCallback;
//				
//			}
//			
//		} catch (InvocationTargetException e){
//			result = e.getTargetException();
//		} catch (IllegalArgumentException e) {
//			result = e;
//		} catch (IllegalAccessException e) {
//			result = e;
//		} 
//		_log.finer("sending answer");
//		TxPacket packet = new TxPacket();
//		packet.setHeader(Statics.INVOCATION_RETURN_PACKET, requestID);
//		
//		packet = Utils.wrapValue(method.getReturnType(), result, packet); // wrap the result
//		packet.setComplete();
//		dispatcher.send(key, packet.getByteBuffer());
//		
//		//Utils.debug("ReadEventHandler.processInvokeMethod() -> end. requestID="+requestID);
//		if (_log.isLoggable(Level.FINE))
//			_log.fine("end. requestID="+requestID);
//	}

}
