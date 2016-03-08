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

/**
 * This class is used by an endpoint if a local endpoint object has to be "transferred" again to it's origin.
 * In such case, only the remote object name, (wrapped in this class) as it is available in the origins lookup table is sent.
 * 
 * @author ACHR
 * @since 1.2.0
 */
class SimonEndpointReference implements Serializable {

    private String remoteObjectName;
    
    SimonEndpointReference(SimonProxy sp) {
        remoteObjectName = sp.getRemoteObjectName();
    }

    String getRemoteObjectName() {
        return remoteObjectName;
    }

    @Override
    public String toString() {
        return "SimonEndpointReference{" + "remoteObjectName=" + remoteObjectName + '}';
    }
    
}
