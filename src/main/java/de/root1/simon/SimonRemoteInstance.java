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

import de.root1.simon.annotation.SimonRemote;
import de.root1.simon.utils.Utils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used by an endpoint if a remote object has to be "transferred" to the 
 * opposite endpoint. In such case, only the interface name is relevant. So an 
 * instance of this class is transferred instead of the "real" implementation of the remote object.
 * 
 * @author ACHR
 */
public class SimonRemoteInstance implements Serializable {

    private static final long serialVersionUID = 1;
    private transient final Logger logger = LoggerFactory.getLogger(getClass());
    /** Name of the interface that is used to implement the remote object */
    private List<String> interfaceNames = new ArrayList<String>();
    /** a unique identifier for the corresponding remote object */
    private String id = null;
    /** the remote object name of the simon proxy to which the SimonRemote belongs */
    private String remoteObjectName = null;

    /**
     *
     * Creates a new SimonRemoteInstance transport object
     *
     * @param session the {@link IoSession} to which the remote object is related to
     * @param remoteObject the remote object for which we generate this transport object for
     */
    protected SimonRemoteInstance(IoSession session, Object remoteObject) {
        logger.debug("begin");

        try {
            remoteObjectName = Simon.getSimonProxy(remoteObject).getRemoteObjectName();
        } catch (IllegalArgumentException e) {
            remoteObjectName = "{SimonRemoteInstance:RemoteObjectNameNotAvailable}";
        }


        String IP = session.getRemoteAddress().toString();
        long sessionId = session.getId();

        StringBuilder sb = new StringBuilder();

        sb.append("[");
        sb.append(remoteObject.getClass().getName());
        sb.append("|ip=");
        sb.append(IP);
        sb.append(";sessionID=");
        sb.append(sessionId);
        sb.append(";remoteObjectHash=");
        sb.append(remoteObject.hashCode());
        sb.append("]");

        this.id = sb.toString();

        logger.debug("SimonRemoteInstance created with id={}", this.id);


        Class[] remoteInterfacesInAnnotation=null;
        boolean isAnnotated = Utils.isRemoteAnnotated(remoteObject);
        if (isAnnotated) {
            SimonRemote annotation = remoteObject.getClass().getAnnotation(SimonRemote.class);
            remoteInterfacesInAnnotation = annotation.value();
            logger.trace("SimonRemoteObject is annotated with SimonRemote");
        }

        // check if we have to look for the annotation or the implemented interfaces
        if (remoteInterfacesInAnnotation!=null && remoteInterfacesInAnnotation.length>0) {
            logger.trace("SimonRemoteObject has defined interfaces in it's annotation");

            for (Class interfaceClazz : remoteInterfacesInAnnotation) {
                String clazzName = interfaceClazz.getCanonicalName();
                logger.trace("Adding {} to the list of remote interfaces", clazzName);
                interfaceNames.add(clazzName);
            }

        } else {

            logger.trace("Need to manually search for remote interfaces ...");

            if (isAnnotated) {
                logger.trace("Getting all (sub)interfaces...");

                Stack<Class> stack = new Stack<Class>();
                
                putInterfacesToStack(stack, remoteObject.getClass());

                while (!stack.empty()) {
                    Class iClazz = stack.pop();
                    String iClazzName = iClazz.getCanonicalName();
                    logger.trace("Adding {} to the list of remote interfaces", iClazzName);
                    if (!interfaceNames.contains(iClazzName)) {
                        interfaceNames.add(iClazzName);
                    }
                    putInterfacesToStack(stack, iClazz);
                }


            } else {
                logger.trace("Searching for explicit remote interfaces marked with {} ...", SimonRemote.class.getName());

                Class[] remoteInterfaces = remoteObject.getClass().getInterfaces();

                // check each interface if THIS is the one which implements "SimonRemote"
                for (Class<?> interfaceClazz : remoteInterfaces) {


                    String remoteObjectInterfaceClassNameTemp = interfaceClazz.getCanonicalName();

                    logger.trace("Checking interfacename='{}' for '{}'", remoteObjectInterfaceClassNameTemp, SimonRemote.class.getName());

                    // Get the interfaces of the implementing interface
                    Class<?>[] remoteObjectInterfaceSubInterfaces = interfaceClazz.getInterfaces();

                    for (Class<?> remoteObjectInterfaceSubInterface : remoteObjectInterfaceSubInterfaces) {

                        logger.trace("Checking child interfaces for '{}': child={}", remoteObjectInterfaceClassNameTemp, remoteObjectInterfaceSubInterface);

                        if (remoteObjectInterfaceSubInterface.getName().equalsIgnoreCase(SimonRemote.class.getName())) {
                            logger.trace("Adding {} to the list of remote interfaces", remoteObjectInterfaceClassNameTemp);
                            if (!interfaceNames.contains(remoteObjectInterfaceClassNameTemp)) {
                                interfaceNames.add(remoteObjectInterfaceClassNameTemp);
                            }
                        }
                    }

                }
            }
        }
        logger.debug("end");
    }

    /**
     * TODO document me ...
     * @param stack
     * @param clazz
     */
    private void putInterfacesToStack(Stack<Class> stack, Class clazz) {
        Class[] interfaces = clazz.getInterfaces();
        for (Class iClazz : interfaces) {
            stack.push(iClazz);
        }
    }


    /**
     *
     * Returns the name of the interface of the remote object's implementation
     *
     * @return the remote object's interface
     */
    protected List<String> getInterfaceNames() {
        return interfaceNames;
    }

    /**
     *
     * Returns an unique identifier for this remote object. This is necessary to differ from two
     * remote objects with the same implementation
     *
     * @return a unique ID for the remote object
     */
    protected String getId() {
        return id;
    }

    /**
     * Returns the proxy's remote object name in the related lookup table
     * @return the remote object name
     */
    protected String getRemoteObjectName() {
        return remoteObjectName;
    }
}
