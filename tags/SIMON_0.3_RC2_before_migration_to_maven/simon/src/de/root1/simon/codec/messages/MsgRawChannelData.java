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

import java.nio.ByteBuffer;

/**
 * <code>RawChannelData</code> message
 *
 * @author ACHR
 */
public class MsgRawChannelData extends AbstractMessage {
	
    private static final long serialVersionUID = 1L;

    /*
     * Just for debugging: Channeltokes are spread over the whole size of integer.
     * So, at beginning, the initial value is 1. But the token generator will take 
     * Integer.MIN_VALUE. So you can see if there's a token problem if at beginning the token size is positive instead of negative.
     */
    private int channelToken = 1;
    private ByteBuffer data;
    
    public MsgRawChannelData() {
    	super(SimonMessageConstants.MSG_RAW_CHANNEL_DATA);
    }

    public int getChannelToken() {
        return channelToken;
    }

    public void setChannelToken(int channelToken) {
        this.channelToken = channelToken;
    }
    
    public void setData(ByteBuffer byteBuffer){
    	this.data = byteBuffer;
    }
    
    public ByteBuffer getData(){
    	return data;
    }

    @Override
    public String toString() {
        // it is a good practice to create toString() method on message classes.
        return getSequence() + ":MsgRawChannelData(channelToken=" + channelToken + "|data="+data+")";
    }

}
