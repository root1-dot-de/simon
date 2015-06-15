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

import java.io.Serializable;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * If an remote-proxy needs to be transferred again, only a reference is transferred.
 * The receiving side detects this, and gives the matching and already known object
 * 
 * @author ACHR
 */
public class SimonRemoteRef implements Serializable {

    private static final long serialVersionUID = 1;
    private transient final Logger logger = LoggerFactory.getLogger(getClass());
    
    /** ID identifying the remote object */
    private String id;

    /**
     *
     * Creates a new SimonRemoteInstance transport object
     *
     * @param session the {@link IoSession} to which the remote object is related to
     * @param remoteObject the remote object for which we generate this transport object for
     */
    protected SimonRemoteRef(IoSession session, Object remoteObject) {
        logger.debug("begin");
        
        String remoteObjectName = null;
        
        /*
         * try to get an name for this object.
         * The name is used by the equals-method in ProcessMessageRunnable to get an instance to compare with.
         * As this does only make sense in case of it's a object that has been explicitly bound to registry, it's save
         * to have a non working remote object name in case of any other implicit remote object
         */
        try {
            remoteObjectName = Simon.getSimonProxy(remoteObject).getRemoteObjectName();
        } catch (IllegalArgumentException e) {
            remoteObjectName = "{SimonRemoteRef:RemoteObjectNameNotAvailable}";
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

        logger.debug("SimonRemoteRef created with id={}", this.id);

        logger.debug("end");
    }

    /**
     *
     * Returns an unique identifier for this remotely referenced object. This is necessary to differ from two
     * remote objects with the same implementation
     *
     * @return a unique ID for the remote object
     */
    protected String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "SimonRemoteRef{" + "id=" + id + '}';
    }

}
