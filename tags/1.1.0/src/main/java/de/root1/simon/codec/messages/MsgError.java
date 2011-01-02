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

/**
 * <code>ERROR</code> message.
 * By default, the error is declared as "read" error, means, error while decoding.
 *
 * @author ACHR
 * @since 1.1.0
 */
public class MsgError extends AbstractMessage {
	
    private static final long serialVersionUID = 1L;

    private String remoteObjectName;
    private String errorMessage;
    private Throwable throwable;
    private int initSequenceId;
    private boolean isDecodeError = true;

    public MsgError() {
    	super(SimonMessageConstants.MSG_ERROR);
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
     * Get the message assiciated with the error
     * @return the errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the error message of MsgError
     * @param errorMessage the errorMessage to set
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Returns the associated throwable/exception. May be null.
     * 
     * @return the throwable
     */
    public Throwable getThrowable() {
        return throwable;
    }

    /**
     * Sets the occured exception
     * @param throwable the throwable to set
     */
    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    /**
     * @return the initSequenceId
     */
    public int getInitSequenceId() {
        return initSequenceId;
    }

    /**
     * @param initSequenceId the initSequenceId to set
     */
    public void setInitSequenceId(int initSequenceId) {
        this.initSequenceId = initSequenceId;
    }
    
    /**
     * 
     */
    public void setEncodeError(){
        isDecodeError = false;
    }
    
    /**
     * 
     */
    public void setDecodeError(){
        isDecodeError = true;
    }
    
    /**
     * Returns true, if error occured while decoding a message
     * Returns false, if error occured while encoding a message
     * @return the isRequestError 
     */
    public boolean isDecodeError() {
        return isDecodeError;
    }

    
    @Override
    public String toString() {
        // it is a good practice to create toString() method on message classes.
        return getSequence() + ":MsgError(ron=" + getRemoteObjectName() + "|errorMessage=" + errorMessage + "|throwable=" + throwable + "|isDecodeError="+isDecodeError+")";
    }

}
