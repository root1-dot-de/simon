package de.root1.simon;

import java.util.concurrent.ThreadFactory;

public class NamedThreadPoolFactory implements ThreadFactory {

	/** the base name for each thread created with this factory */
	private String baseName;

	/**
	 * Creates a new thread-factory that gives each thread a basename
	 * 
	 * @param baseName the basename for the created threads
	 */
	public NamedThreadPoolFactory(String baseName) {
		this.baseName = baseName;
	}
	
	public Thread newThread(Runnable r) {
		return new Thread(r,"["+baseName+"->"+r+"]");
	}

}



