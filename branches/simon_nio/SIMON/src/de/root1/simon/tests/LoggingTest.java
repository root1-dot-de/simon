/**
 * TODO Documentation to be done
 */
package de.root1.simon.tests;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TODO Documentation to be done
 *
 * @author achristian
 *
 */
public class LoggingTest {
	
	public static void main(String[] args) throws InterruptedException {
		Logger _log = Logger.getLogger("de.root1.simon");

		ConsoleHandler    fh = new ConsoleHandler( );

		fh.setLevel(Level.FINEST);
	    _log.addHandler( fh );
	    
		_log.setLevel(Level.FINEST);
		
		_log.log(Level.FINER, "Simon lib loaded");
	}

}
