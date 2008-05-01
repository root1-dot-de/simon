package de.root1.simon;

import java.util.concurrent.ThreadFactory;

/**
 * A simple factory-class that let's you use named threads in a thread-pool
 * TODO Documentation to be done
 *
 * @author achristian
 *
 */
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
	
	/*
	 * (non-Javadoc)
	 * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
	 */
	public Thread newThread(Runnable r) {
		return new Thread(r,"["+baseName+"->"+r+"]");
	}

}



