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

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple monitor class whose instance can have a sequence id.
 * @author achr
 *
 */
public class Monitor {

    private final Semaphore s = new Semaphore(1);
    /**
     * the associated sequence id
     */
    private final int sequenceId;

    /**
     * Creates a monitor object
     * @param sequenceId the associated sequence id
     */
    protected Monitor(int sequenceId) {
        this.sequenceId = sequenceId;
        try {
            s.acquire();
        } catch (InterruptedException ex) {
        }
    }

    /**
     * Returns the associated sequence id
     * @return the id
     */
    protected int getSequenceId() {
        return sequenceId;
    }

    /**
     * Waits for a signal. if it becomes available within the given waiting 
     * time, true will be returned. Otherwise you will get false
     * 
     * @param waiting time for signal to receive
     * @return true, if signal received, false if not
     * 
     */
    public boolean waitForSignal(long timeout) {
        try {
            return s.tryAcquire(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            return false;
        }
    }

    /**
     * Provide the signal
     */
    public void signal() {
        s.release();
    }
}
