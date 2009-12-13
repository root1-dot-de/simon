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
 * An interface for getting some network related statistics about the SIMON {@link Registry}
 * 
 * @author alexanderchristian
 *
 */
public interface SimonRegistryStatistics extends SimonRemoteStatistics {

    /**
     * Returns the maximum number of sessions which were being managed at the
     * same time.
     */
    public abstract int getLargestManagedSessionCount();

    /**
     * Returns the cumulative number of sessions which were managed (or are
     * being managed) by this service, which means 'currently managed session
     * count + closed session count'.
     */
    public abstract long getCumulativeManagedSessionCount();

    /**
     * Returns the maximum of the {@link #getReadBytesThroughput() readBytesThroughput}.
     */
    public abstract double getLargestReadBytesThroughput();

    /**
     * Returns the maximum of the {@link #getWrittenBytesThroughput() writtenBytesThroughput}.
     */
    public abstract double getLargestWrittenBytesThroughput();

    /**
     * Returns the maximum of the {@link #getReadMessagesThroughput() readMessagesThroughput}.
     */
    public abstract double getLargestReadMessagesThroughput();

    /**
     * Returns the maximum of the {@link #getWrittenMessagesThroughput() writtenMessagesThroughput}.
     */
    public abstract double getLargestWrittenMessagesThroughput();
}
