package de.root1.simon;

import java.io.Serializable;


public class SimonCallback implements Serializable {
	
	/**
	 * TODO: Documentation to be done for field 'serialVersionUID', by 'ACHR'..
	 *
	 */
	private static final long serialVersionUID = -1613858098177523543L;
	
	/**
	 * Name of the interface that is used to implement the callback-object
	 */
	private String interfaceName = null;
	private String id = null;

	/**
	 * 
	 * TODO: Documentation to be done for constructor 'SimonCallback', by 'ACHR'..
	 * 
	 * @param callback
	 */
	public SimonCallback(SimonRemote callback) {
		if (Statics.DEBUG_MODE)
			System.out.println("SimonCallback.SimonCallback() -> start");
		this.id = callback.toString();

		// get the interfaces the arg has implemented
		Class<?>[] callbackInterfaceClasses = callback.getClass().getInterfaces();

		// check each interface if THIS is the one which implements "SimonRemote"
		for (Class<?> callbackInterfaceClass : callbackInterfaceClasses) {
			

			String callbackInterfaceClassNameTemp = callbackInterfaceClass.getName();
			if (Statics.DEBUG_MODE)
				System.out.println("SimonCallback.SimonCallback() -> Checking interfacename='"+callbackInterfaceClassNameTemp+"' for '"+SimonRemote.class.getName()+"'");
			
			// Get the interfaces of the implementing interface
			Class<?>[] callbackInterfaceSubInterfaces = callbackInterfaceClass.getInterfaces();
			
			boolean isSimonRemote = false;
			for (Class<?> callbackInterfaceSubInterface : callbackInterfaceSubInterfaces) {
				if (Statics.DEBUG_MODE)
					System.out.println("SimonCallback.SimonCallback() -> Checking child interfaces for '"+callbackInterfaceClassNameTemp+"': child="+callbackInterfaceSubInterface);
				if (callbackInterfaceSubInterface.getName().equalsIgnoreCase(SimonRemote.class.getName())) {
					isSimonRemote = true;
					break;
				}
			}
			
			if (isSimonRemote){
				interfaceName = callbackInterfaceClassNameTemp;
				if (Statics.DEBUG_MODE)
					System.out.println("SimonCallback.SimonCallback() -> SimonRemote found in arg: interfaceName='"+interfaceName+"'");
				break;

			} else {
				interfaceName = null;
			}
		}
		if (Statics.DEBUG_MODE)
			System.out.println("SimonCallback.SimonCallback() -> end");
	}

	/**
	 * 
	 * TODO: Documentation to be done for method 'getInterfaceName', by 'ACHR'..
	 * 
	 * @return
	 */
	public String getInterfaceName() {
		return interfaceName;
	}
	
	/**
	 * 
	 * TODO: Documentation to be done for method 'getInterfaceName', by 'ACHR'..
	 * 
	 * @return
	 */
	public String getId() {
		return id;
	}
	


}
