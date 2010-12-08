/*
 * Copyright (C) 2008 Alexander Christian <alex(at)root1.de>. All rights reserved.
 *
 * This file is part of SIMON.
 *
 *   SIMON is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SIMON is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with SIMON.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.root1.simon;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This is a marker-interface to mark a class as a remote class
 * @author achr
 *
 * @since 1.1.0
 */
public class SimonRemoteMarker implements InvocationHandler {

    private transient final Logger logger = LoggerFactory.getLogger(getClass());

    /** TODO: document me! */
    private final Object objectToBeMarked;
    
    /**
     * TODO document me!
     * @param objectToBeMarked
     */
    protected SimonRemoteMarker(Object objectToBeMarked) {
        this.objectToBeMarked = objectToBeMarked;
    }

    /**
     * TODO document me!
     * @return objectToBeMarked
     */
    protected Object getObjectToBeMarked(){
        return objectToBeMarked;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        logger.trace("objectToBeMarked={} method={} args.length={}", new Object[]{objectToBeMarked.getClass().getCanonicalName(), method.getName(), (args==null? 0 : args.length)});
        return method.invoke(objectToBeMarked, args);
    }

    

}
