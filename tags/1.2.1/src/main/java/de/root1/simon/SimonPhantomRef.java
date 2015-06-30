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

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import org.apache.mina.core.session.IoSession;

/**
 * Phantom Reference for SIMON callback remote objects. With help of that, user
 * must not release callback object. This is done automatically.
 *
 * @author achristian
 * @since 1.2.0
 */
public class SimonPhantomRef<T extends SimonProxy> extends PhantomReference<T> {

    private String refId;
    private final IoSession session;

    public SimonPhantomRef(T referent, ReferenceQueue<? super T> q) {
        super(referent, q);
        refId = referent.getRemoteObjectName();
        session = referent.getIoSession();
    }

    String getRefId() {
        return refId;
    }

    IoSession getSession() {
        return session;
    }

    @Override
    public String toString() {
        return "SimonPhantomRef{" + "refId=" + refId + ", session=" + session + '}';
    }
}
