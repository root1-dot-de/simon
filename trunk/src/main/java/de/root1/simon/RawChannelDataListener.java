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

import de.root1.simon.exceptions.RawChannelException;
import java.nio.ByteBuffer;

/**
 * Interface to be implemented for receiving data ia RawChannel.
 * 
 * @author achristian
 */
public interface RawChannelDataListener {

    /**
     * Called by SIMON when data is received through a RawChannel.
     * Implementation is responsible for processing/storing/... received data.
     * 
     * @param data ByteBuffer with received data
     * @throws RawChannelException if an error occurs while writing data
     */
    void write(ByteBuffer data) throws RawChannelException;

    /**
     * Called by SIMON when a RawChannel is closed.
     * Implementation can use this to close filestreams or whetever is needed to process/Store/... received data.
     * @throws RawChannelException  if an error occurs while closing.
     */
    void close() throws RawChannelException;
}
