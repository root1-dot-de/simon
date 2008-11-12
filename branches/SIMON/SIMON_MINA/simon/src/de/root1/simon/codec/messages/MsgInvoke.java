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
package de.root1.simon.codec.messages;

import java.lang.reflect.Method;


/**
 * <code>INVOKE</code> message
 *
 * @author ACHR
 */
public class MsgInvoke extends AbstractMessage {
	
    private static final long serialVersionUID = 1L;

    String remoteObjectName;
    Method method;
    Object[] args;
    
    public MsgInvoke() {
    	super(SimonMessageConstants.MSG_INVOKE);
    }

    public String getRemoteObjectName() {
        return remoteObjectName;
    }

    public void setRemoteObjectName(String remoteObjectName) {
        this.remoteObjectName = remoteObjectName;
    }
    
    public void setArguments(Object[] args){
    	this.args=args;
    }

    @Override
    public String toString() {
        // it is a good practice to create toString() method on message classes.
        return getSequence() + ":MsgInvoke(ron=" + remoteObjectName + "|method=" + method + "|args=" + args + ")";
    }

	public void setMethod(Method method) {
		this.method = method;
	}
	
	public Method getMethod(){
		return method;
	}
	
	public Object[] getArguments(){
		return args;
	}
}
