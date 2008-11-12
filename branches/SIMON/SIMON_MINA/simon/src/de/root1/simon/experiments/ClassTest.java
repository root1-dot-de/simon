package de.root1.simon.experiments;

import de.root1.simon.Simon;
import de.root1.simon.codec.base.SimonProtocolCodecFactory;

public class ClassTest extends SimonProtocolCodecFactory {
	
	public static void main(String[] args)  {
		
		try {
			Simon.setProtocolCodecFactory("de.root1.simon.Simon");
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}
