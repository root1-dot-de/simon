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

import org.apache.mina.core.session.IoSession;

/**
 * A simple implementation of {@link SimonRemoteStatistics}
 * 
 * @author alexanderchristian
 *
 */
public class RemoteStatistics implements SimonRemoteStatistics {

    private IoSession ioSession;

    protected RemoteStatistics(IoSession ioSession) {
        this.ioSession = ioSession;
    }


    /* (non-Javadoc)
     * @see de.root1.simon.SimonRemoteStatistics#getLastIoTime()
     */
    public long getLastIoTime() {
        return ioSession.getLastIoTime();
    }

    /* (non-Javadoc)
     * @see de.root1.simon.SimonRemoteStatistics#getLastReadTime()
     */
    public long getLastReadTime() {
        return ioSession.getLastReadTime();
    }

    /* (non-Javadoc)
     * @see de.root1.simon.SimonRemoteStatistics#getLastWriteTime()
     */
    public long getLastWriteTime() {
        return ioSession.getLastWriteTime();
    }

    /* (non-Javadoc)
     * @see de.root1.simon.SimonRemoteStatistics#getReadBytes()
     */
    public long getReadBytes() {
        return ioSession.getReadBytes();
    }

    /* (non-Javadoc)
     * @see de.root1.simon.SimonRemoteStatistics#getReadBytesThroughput()
     */
    public double getReadBytesThroughput() {
        return ioSession.getReadBytesThroughput();
    }

    /* (non-Javadoc)
     * @see de.root1.simon.SimonRemoteStatistics#getReadMessages()
     */
    public long getReadMessages() {
        return ioSession.getReadMessages();
    }

    /* (non-Javadoc)
     * @see de.root1.simon.SimonRemoteStatistics#getReadMessagesThroughput()
     */
    public double getReadMessagesThroughput() {
        return ioSession.getReadMessagesThroughput();
    }

    /* (non-Javadoc)
     * @see de.root1.simon.SimonRemoteStatistics#getScheduledWriteBytes()
     */
    public long getScheduledWriteBytes() {
        return ioSession.getScheduledWriteBytes();
    }

    /* (non-Javadoc)
     * @see de.root1.simon.SimonRemoteStatistics#getScheduledWriteMessages()
     */
    public long getScheduledWriteMessages() {
        return ioSession.getScheduledWriteMessages();
    }

    /* (non-Javadoc)
     * @see de.root1.simon.SimonRemoteStatistics#getWrittenBytes()
     */
    public long getWrittenBytes() {
        return ioSession.getWrittenBytes();
    }

    /* (non-Javadoc)
     * @see de.root1.simon.SimonRemoteStatistics#getWrittenBytesThroughput()
     */
    public double getWrittenBytesThroughput() {
        return ioSession.getWrittenBytesThroughput();
    }

    /* (non-Javadoc)
     * @see de.root1.simon.SimonRemoteStatistics#getWrittenMessages()
     */
    public long getWrittenMessages() {
        return ioSession.getWrittenMessages();
    }

    /* (non-Javadoc)
     * @see de.root1.simon.SimonRemoteStatistics#getWrittenMessagesThroughput()
     */
    public double getWrittenMessagesThroughput() {
        return ioSession.getWrittenMessagesThroughput();
    }
}
