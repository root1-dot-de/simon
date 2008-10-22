package de.root1.simon;

import java.util.logging.Logger;

import org.apache.mina.core.session.IoSession;

import de.root1.simon.codec.messages.AbstractMessage;
import de.root1.simon.codec.messages.MsgLookup;
import de.root1.simon.codec.messages.MsgLookupReturn;
import de.root1.simon.exceptions.LookupFailedException;

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
		System.out.println("ProcessMessagePool: "+abstractMessage);
		
		if (abstractMessage instanceof MsgLookup) {
			try {
				processLookup(abstractMessage);
			} catch (LookupFailedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} else if (abstractMessage instanceof MsgLookupReturn) {
			processLookupResult(abstractMessage);
		}
		
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
	/**
	 * 
	 * processes a lookup
	 * @throws LookupFailedException 
	 */
	private void processLookup(AbstractMessage abstractMessage) throws LookupFailedException {
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
	
	private void processLookupResult(AbstractMessage abstractMessage2) {
		_log.fine("begin");
		_log.fine("processing MsgLookupReturn...");	
		MsgLookupReturn msg = (MsgLookupReturn)abstractMessage;
		Class<?>[] interfaces = msg.getInterfaces();
		_log.fine("Forward result to waiting monitor");
		_log.fine("remoteObjectName="+interfaces);
		dispatcher.putResultToQueue(msg.getSequence(), msg);
		_log.fine("end");
	}

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
