/*
 * Copyright (C) 2012 Alexander Christian <alex(at)root1.de>. All rights reserved.
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

import java.util.List;

/**
 * JMX Interface for LookupTable
 * @author achristian
 * @since 1.2.0
 */
public interface LookupTableMBean {
    
    public static final String MBEAN_SUBTYPE_SERVER = "server";
    public static final String MBEAN_SUBTYPE_CLIENT = "client";
    public static final String MBEAN_TYPE = "LookupTable";
    
    public int getNumberOfRemoteRefSessions();
    public Long[] getRemoteRefSessions();
    public String[] getRefIdsForSession(long sessionId);
    public int getRemoteRefCount(long sessionId, String refId);
    
    /**
     * Number of references for all sessions and all remote objects
     * @return total ref count
     */
    public int getTotalRefCount();
    
    public List<String> getCallbackRefList();
    
}
