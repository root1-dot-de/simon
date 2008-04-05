package de.root1.simon.nioexample.interfaces;


/**
 * 
 * Creates InputHandlers
 *
 */
public interface IInputHandlerFactory {

	/**
	 * 
	 * Returns a new InputHandler
	 * @return
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	IInputHandler newHandler() throws IllegalAccessException, InstantiationException;
}
