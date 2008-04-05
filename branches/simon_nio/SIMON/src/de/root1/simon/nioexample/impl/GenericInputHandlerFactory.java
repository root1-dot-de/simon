package de.root1.simon.nioexample.impl;

import de.root1.simon.nioexample.interfaces.IInputHandler;
import de.root1.simon.nioexample.interfaces.IInputHandlerFactory;

/**
 * 
 * TODO Documentation to be done
 *
 */
public class GenericInputHandlerFactory implements IInputHandlerFactory {
	
	private final Class<? extends IInputHandler> handlerClass;

	public GenericInputHandlerFactory(Class<? extends IInputHandler> handlerClass) {
		this.handlerClass = handlerClass;
	}

	public IInputHandler newHandler() throws IllegalAccessException,
			InstantiationException {
		return handlerClass.newInstance();
	}
}
