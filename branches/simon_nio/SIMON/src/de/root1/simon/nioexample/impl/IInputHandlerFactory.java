package de.root1.simon.nioexample.impl;

import de.root1.simon.nioexample.interfaces.IInputHandler;

public interface IInputHandlerFactory {
	IInputHandler newHandler() throws IllegalAccessException,
			InstantiationException;
}
