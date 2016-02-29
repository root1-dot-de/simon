/*
 * Copyright (C) 2012 Alexander Christian <alex(at)root1.de>. All rights reserved.
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

import de.root1.simon.exceptions.SessionException;
import de.root1.simon.exceptions.SimonException;
import de.root1.simon.utils.Utils;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A ReferenceQueue that tracks lifetime of SimonProxy objects. If proxy is
 * GC'ed, a message is sent to remote to signal that the reference-count can be
 * decreased by 1.
 *
 * @author achristian
 * @param <T>
 * @since 1.2.0
 */
public class SimonRefQueue <T extends SimonPhantomRef> extends ReferenceQueue<T> implements Runnable {
    
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final List<Reference> refs = new ArrayList<Reference>();
    private static final int REMOVE_TIMEOUT = 5000;
    private final Thread refCleanerThread;
    private final Dispatcher dispatcher;

    SimonRefQueue(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
        refCleanerThread = new Thread(this, "SimonRefQueue#"+hashCode());
        refCleanerThread.setDaemon(true);
        refCleanerThread.start();
    }

    public synchronized void addRef(SimonProxy simonProxy) {
        if (!refCleanerThread.isAlive() || refCleanerThread.isInterrupted()) {
            throw new IllegalStateException("refCleanerThread not longer active. Shutdown in progress?");
        }
        Reference ref = new SimonPhantomRef(simonProxy, this);
        logger.debug("Adding ref: {}", ref);
        refs.add(ref);
        logger.debug("Ref count after add: {}",refs.size());
    }

    @Override
    public void run() {
        while (!refCleanerThread.isInterrupted()) {
            try {
                SimonPhantomRef ref = (SimonPhantomRef) remove(REMOVE_TIMEOUT);
                if (ref != null) {
                    logger.debug("Releasing: {}",ref);
                    refs.remove(ref);
                    ref.clear();
                    logger.debug("Ref count after remove: {}", refs.size());
                    sendRelease(ref);
                } else {
                    // FIXME remove GC call here ...
//                    if (logger.isTraceEnabled()) {
//                        logger.trace("********** Trigger GC! **********");
//                        System.gc();
//                    }
                }
            } catch (InterruptedException ex) {
                refCleanerThread.interrupt();
            }
        }
        
        logger.debug(Thread.currentThread().getName()+" terminated");
    }

    synchronized void cleanup() {
        logger.debug("Stopping refCleanerThread");
        refCleanerThread.interrupt();
        
        logger.debug("Sending release for {} refs", refs.size());
        while (!refs.isEmpty()) {
            // remove one by one until list is empty
            SimonPhantomRef ref = (SimonPhantomRef) refs.remove(0);
            sendRelease(ref);
        }
        // ensure it is cleared
        refs.clear();
    }

    private void sendRelease(SimonPhantomRef ref) {
        try {
            if (ref.getSession().isConnected()) {
                dispatcher.sendReleaseRef(ref.getSession(), ref.getRefId());
            } else {
                logger.debug("Sending release for ref {} not possible due to closed session {}.", ref, Utils.longToHexString(ref.getSession().getId()));
            }
        } catch (SimonException ex) {
            logger.warn("Not able to send a 'release ref' for "+ref, ex);
        } catch (SessionException ex) {
            logger.warn("Not able to send a 'release ref' for "+ref, ex);
        }
    }
}