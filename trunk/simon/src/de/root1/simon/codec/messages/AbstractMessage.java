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
import java.io.Serializable;

import de.root1.simon.Statics;

/**
 * A base message for SIMON protocol messages.
 *
 * @author ACHR
 */
public abstract class AbstractMessage implements Serializable {
    
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private byte msgType = -1;
	private int sequence = -1;
	private String errorMsg = Statics.NO_ERROR;
	
    /**
     * Returns the error message. Contains {@link Statics#NO_ERROR} if no error is present.
	 * @return the errorMsg
	 */
	public String getErrorMsg() {
		return errorMsg;
	}

	/**
	 * Sets the error message related to this message
	 * @param errorMsg the errorMsg to set
	 */
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	
	/**
	 * Returns whether this message has an error or not
	 * @return true, if error is present, false if not
	 */
	public boolean hasError(){
		return (!errorMsg.equals(Statics.NO_ERROR));
	}

	/**
     * Creates a new message decoder
     * @param msgType specifies a unique ID for the type of message
     */
    protected AbstractMessage(byte msgType) {
        this.msgType = msgType;
    }
	
	/**
	 * Returns the message type as described by {@link SimonMessageConstants}
	 * @return the msgType
	 */
	public int getMsgType() {
		return msgType;
	}

	/**
	 * Returns, guess what, the sequence id of the message
	 * @return the sequence
	 */
	public int getSequence() {
		return sequence;
	}
	
	/**
	 * Stores the sequence id in the message
	 * @param sequence the sequence to set
	 */
	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

}