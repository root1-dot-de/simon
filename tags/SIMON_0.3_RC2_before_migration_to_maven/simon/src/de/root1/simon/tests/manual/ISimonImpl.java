package de.root1.simon.tests.manual;

import de.root1.simon.exceptions.SimonRemoteException;

class ISimonImpl implements IServer {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public ISimonImpl() {
			// TODO Auto-generated constructor stub
		}

		public String helloServerWorld() throws SimonRemoteException {
			System.out.println("Hello World on Server");
			return "Hello World on Client";
		}

}