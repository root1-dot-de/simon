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

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Container holding a reference to an callback object as well as a reference counter.
 * Used by LookupTable to store callbacks as long as remote side does not GC the callbacks proxy object.
 * 
 * @author achristian
 * @since 1.2.0
 */
class RemoteRefContainer {
        
        private final AtomicInteger refCount = new AtomicInteger(1);
        private final Object object;

        RemoteRefContainer(Object object) {
            this.object = object;
        }

        public int getRefCount() {
            return refCount.get();
        }
        
        public int addRef() {
            return refCount.incrementAndGet();
        }
        
        public int removeRef() {
            return refCount.decrementAndGet();
        }

        public Object getObject() {
            return object;
        }

        @Override
        public String toString() {
            return "RemoteRef{" + "refCount=" + refCount + ", object=" + object + '}';
        }
        
    }
