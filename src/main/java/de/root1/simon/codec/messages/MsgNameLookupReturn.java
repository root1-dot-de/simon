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
package de.root1.simon.codec.messages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.root1.simon.Statics;

/**
 * Lookup return message
 *
 * @author ACHR
 */
public class MsgNameLookupReturn extends AbstractMessage {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final long serialVersionUID = 1L;
    private String[] interfaces;
    private String errorMsg = Statics.NO_ERROR;

    public MsgNameLookupReturn() {
        super(SimonMessageConstants.MSG_NAME_LOOKUP_RETURN);

        // dummy init so that on case of an error no "null" has top be transferred
//        interfaces = new Class<?>[1];
//        interfaces[0] = Object.class;
        interfaces = new String[1];
        interfaces[0] = Object.class.getCanonicalName();
        logger.trace("interfaces.length={}", interfaces.length);
    }

    public Class<?>[] getInterfaces() throws ClassNotFoundException {
        return getInterfaces(this.getClass().getClassLoader());
    }

    public Class<?>[] getInterfaces(ClassLoader cl) throws ClassNotFoundException {
        try {
            Class<?>[] interfaceClasses = new Class<?>[interfaces.length];
            for (int i = 0; i < interfaceClasses.length; i++) {
                interfaceClasses[i] = Class.forName(interfaces[i], true, cl);
            }
            return interfaceClasses;
        } catch (ClassNotFoundException ex) {
            throw new ClassNotFoundException("Failed loading remote interface class(es) with classloader ["+cl+"].", ex);
        }
    }

    public String[] getInterfacesString() {
        return interfaces;
    }

    public void setInterfaces(String[] interfaces) {
        this.interfaces = interfaces;
    }

    @Override
    public String toString() {
        return getSequence() + ":MsgNameLookupReturn(interface=" + interfaces + "|errorMsg=" + errorMsg + ")";
    }
}
