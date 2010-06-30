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
 * An interface for getting some network related statistics from a {@link SimonRemote}'s session
 * 
 * @author alexanderchristian
 *
 */
public interface SimonRemoteStatistics {

	/**
	 * Returns the time in milliseconds when I/O occurred lastly.
	 */
	public abstract long getLastIoTime();

	/**
	 * Returns the time in milliseconds when read operation occurred lastly.
	 */
	public abstract long getLastReadTime();

	/**
	 * Returns the time in milliseconds when write operation occurred lastly.
	 */
	public abstract long getLastWriteTime();

	/**
	 * Returns the number of bytes read by this service
	 *
	 * @return
	 *     The number of bytes this service has read
	 */
	public abstract long getReadBytes();

	/**
	 * Returns the number of bytes written out by this service
	 *
	 * @return
	 *     The number of bytes this service has written
	 */
	public abstract long getWrittenBytes();

	/**
	 * Returns the number of read bytes per second.
	 */
	public abstract double getReadBytesThroughput();

	/**
	 * Returns the number of written bytes per second.
	 */
	public abstract double getWrittenBytesThroughput();
	
	/**
	 * Returns the number of messages this services has read
	 *
	 * @return
	 *     The number of messages this services has read
	 */
	public abstract long getReadMessages();

	/**
	 * Returns the number of messages this service has written
	 *
	 * @return
	 *     The number of messages this service has written
	 */
	public abstract long getWrittenMessages();
	
	/**
	 * Returns the number of read messages per second.
	 */
	public abstract double getReadMessagesThroughput();

	/**
	 * Returns the number of written messages per second.
	 */
	public abstract double getWrittenMessagesThroughput();
	
	/**
	 * Returns the number of bytes that are scheduled for sending 
	 * @return number of bytes scheduled
	 */
	public abstract long getScheduledWriteBytes();

	/**
	 * Returns the number of messages that are scheduled for sending
	 * @return number of messages scheduled
	 */
	public abstract long getScheduledWriteMessages();

}