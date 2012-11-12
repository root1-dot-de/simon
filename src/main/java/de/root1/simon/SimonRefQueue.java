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

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author achristian
 * @since 1.2.0
 */
public class SimonRefQueue <T extends SimonPhantomRef> extends ReferenceQueue<T> implements Runnable {
    
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private List<Reference> refs = new ArrayList<Reference>();
    private static final int REMOVE_TIMEOUT = 5000;
    private Thread refCleanerThread;
    private final Dispatcher dispatcher;

    SimonRefQueue(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
        refCleanerThread = new Thread(this, "SimonRefQueue#"+hashCode());
        refCleanerThread.setDaemon(true);
        refCleanerThread.start();
    }

    public void addRef(SimonProxy simonProxy) {
        Reference ref = new SimonPhantomRef(simonProxy, this);
        logger.debug("Adding ref for: {}", ref);
        refs.add(ref);
        logger.debug("Ref count after add: {}",refs.size());
    }

    @Override
    public void run() {
        while (!refCleanerThread.isInterrupted()) {
            try {
                SimonPhantomRef ref = (SimonPhantomRef) remove(REMOVE_TIMEOUT);
                if (ref != null) {
                    logger.debug("Got ref for GC: {}",ref);
                    refs.remove(ref);
                    ref.clear();
                    logger.debug("Ref count after remove: {}", refs.size());
                    dispatcher.sendReleaseRef(ref.getSession(), ref.getRefId());
                } else {
                    // FIXME remove GC call here ...
                    if (logger.isTraceEnabled()) {
                        logger.trace("********** Trigger GC! **********");
                        System.gc();
                    }
                }
            } catch (InterruptedException ex) {
                refCleanerThread.interrupt();
            }
        }
        
        logger.debug(Thread.currentThread().getName()+" terminated");
    }
}