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

import org.apache.mina.core.service.IoServiceStatistics;

/**
 * A simple implementation of {@link SimonRegistryStatistics}
 * 
 * @author alexanderchristian
 *
 */
public class RegistryStatistics implements SimonRegistryStatistics {

    private IoServiceStatistics ioServiceStatistics;

    protected RegistryStatistics(IoServiceStatistics ioServiceStatistics) {
        this.ioServiceStatistics = ioServiceStatistics;
    }

    /* (non-Javadoc)
     * @see de.root1.simon.SimonStatistics#getLargestManagedSessionCount()
     */
    public int getLargestManagedSessionCount() {
        return ioServiceStatistics.getLargestManagedSessionCount();
    }

    /* (non-Javadoc)
     * @see de.root1.simon.SimonStatistics#getCumulativeManagedSessionCount()
     */
    public long getCumulativeManagedSessionCount() {
        return ioServiceStatistics.getCumulativeManagedSessionCount();
    }

    /* (non-Javadoc)
     * @see de.root1.simon.SimonStatistics#getLastIoTime()
     */
    public long getLastIoTime() {
        return ioServiceStatistics.getLastIoTime();
    }

    /* (non-Javadoc)
     * @see de.root1.simon.SimonStatistics#getLastReadTime()
     */
    public long getLastReadTime() {
        return ioServiceStatistics.getLastReadTime();
    }

    /* (non-Javadoc)
     * @see de.root1.simon.SimonStatistics#getLastWriteTime()
     */
    public long getLastWriteTime() {
        return ioServiceStatistics.getLastWriteTime();
    }

    /* (non-Javadoc)
     * @see de.root1.simon.SimonStatistics#getReadBytes()
     */
    public long getReadBytes() {
        return ioServiceStatistics.getReadBytes();
    }

    /* (non-Javadoc)
     * @see de.root1.simon.SimonStatistics#getWrittenBytes()
     */
    public long getWrittenBytes() {
        return ioServiceStatistics.getWrittenBytes();
    }

    /* (non-Javadoc)
     * @see de.root1.simon.SimonStatistics#getReadMessages()
     */
    public long getReadMessages() {
        return ioServiceStatistics.getReadMessages();
    }

    /* (non-Javadoc)
     * @see de.root1.simon.SimonStatistics#getWrittenMessages()
     */
    public long getWrittenMessages() {
        return ioServiceStatistics.getWrittenMessages();
    }

    /* (non-Javadoc)
     * @see de.root1.simon.SimonStatistics#getReadBytesThroughput()
     */
    public double getReadBytesThroughput() {
        return ioServiceStatistics.getReadBytesThroughput();
    }

    /* (non-Javadoc)
     * @see de.root1.simon.SimonStatistics#getWrittenBytesThroughput()
     */
    public double getWrittenBytesThroughput() {
        return ioServiceStatistics.getWrittenBytesThroughput();
    }

    /* (non-Javadoc)
     * @see de.root1.simon.SimonStatistics#getReadMessagesThroughput()
     */
    public double getReadMessagesThroughput() {
        return ioServiceStatistics.getReadMessagesThroughput();
    }

    /* (non-Javadoc)
     * @see de.root1.simon.SimonStatistics#getWrittenMessagesThroughput()
     */
    public double getWrittenMessagesThroughput() {
        return ioServiceStatistics.getWrittenMessagesThroughput();
    }

    /* (non-Javadoc)
     * @see de.root1.simon.SimonStatistics#getLargestReadBytesThroughput()
     */
    public double getLargestReadBytesThroughput() {
        return ioServiceStatistics.getLargestReadBytesThroughput();
    }

    /* (non-Javadoc)
     * @see de.root1.simon.SimonStatistics#getLargestWrittenBytesThroughput()
     */
    public double getLargestWrittenBytesThroughput() {
        return ioServiceStatistics.getLargestWrittenBytesThroughput();
    }

    /* (non-Javadoc)
     * @see de.root1.simon.SimonStatistics#getLargestReadMessagesThroughput()
     */
    public double getLargestReadMessagesThroughput() {
        return ioServiceStatistics.getLargestReadMessagesThroughput();
    }

    /* (non-Javadoc)
     * @see de.root1.simon.SimonStatistics#getLargestWrittenMessagesThroughput()
     */
    public double getLargestWrittenMessagesThroughput() {
        return ioServiceStatistics.getLargestWrittenMessagesThroughput();
    }

    /**
     * Returns the interval (seconds) between each throughput calculation.
     * The default value is <tt>3</tt> seconds.
     */
    public int getThroughputCalculationInterval() {
        return ioServiceStatistics.getThroughputCalculationInterval();
    }

    /**
     * Returns the interval (milliseconds) between each throughput calculation.
     */
    public long getThroughputCalculationIntervalInMillis() {
        return ioServiceStatistics.getThroughputCalculationIntervalInMillis();
    }

    /**
     * Sets the interval (seconds) between each throughput calculation.
     */
    public void setThroughputCalculationInterval(int throughputCalculationInterval) {
        ioServiceStatistics.setThroughputCalculationInterval(throughputCalculationInterval);
    }

    /* (non-Javadoc)
     * @see de.root1.simon.SimonStatistics#getScheduledWriteBytes()
     */
    public long getScheduledWriteBytes() {
        return ioServiceStatistics.getScheduledWriteBytes();
    }

    /* (non-Javadoc)
     * @see de.root1.simon.SimonStatistics#getScheduledWriteMessages()
     */
    public long getScheduledWriteMessages() {
        return ioServiceStatistics.getScheduledWriteMessages();
    }
}
