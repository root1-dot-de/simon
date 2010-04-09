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
package de.root1.simon.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Declares the remote simon class. Can only be used on the implementation class.
 * When value() is specified, the value defines the remote interfaces to use. Only those interfaces can then be used remotely.
 * If value() is not specified, all known interfaces can be used remotely.
 *
 * @author achristian
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface SimonRemote {

    /**
     * When value() is specified, the value defines the remote interfaces to use. Only those interfaces can then be used remotely.
     * If value() is not specified, all known interfaces can be used remotely.
     * @return the remote interface array
     */
    public abstract java.lang.Class[] value() default {};

}
