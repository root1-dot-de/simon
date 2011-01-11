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

/**
 *
 * A simple class that builds a 3-tupel of
 * <ol>
 * <li>remote object instance</li>
 * <li>remote object name</li>
 * <li>remote object interfaces</li>
 * </ol>
 * @author ACHR
 */
public class RemoteObjectContainer {
    private Object remoteObject;
    private String remoteObjectName;
    private Class<?>[] remoteObjectInterfaces;

    public RemoteObjectContainer(Object remoteObject, String remoteObjectName, Class<?>[] remoteObjectInterfaces) {
        this.remoteObject = remoteObject;
        this.remoteObjectName = remoteObjectName;
        this.remoteObjectInterfaces = remoteObjectInterfaces;
    }

    /**
     * @return the remoteObject
     */
    public Object getRemoteObject() {
        return remoteObject;
    }

    /**
     * @param remoteObject the remoteObject to set
     */
    public void setRemoteObject(Object remoteObject) {
        this.remoteObject = remoteObject;
    }

    /**
     * @return the remoteObjectName
     */
    public String getRemoteObjectName() {
        return remoteObjectName;
    }

    /**
     * @param remoteObjectName the remoteObjectName to set
     */
    public void setRemoteObjectName(String remoteObjectName) {
        this.remoteObjectName = remoteObjectName;
    }

    /**
     * @return the remoteObjectInterfaces
     */
    public Class<?>[] getRemoteObjectInterfaces() {
        return remoteObjectInterfaces;
    }

    /**
     * @param remoteObjectInterfaces the remoteObjectInterfaces to set
     */
    public void setRemoteObjectInterfaces(Class<?>[] remoteObjectInterfaces) {
        this.remoteObjectInterfaces = remoteObjectInterfaces;
    }



}
