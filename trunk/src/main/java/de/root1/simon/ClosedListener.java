package de.root1.simon;

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
/**
 * A Listener interface that gets a closed event on a server remote object
 * 
 * @author achristian
 * @version 200903041505
 */
public interface ClosedListener {

    /**
     * Called whenever a server remote object is not useable anymore. This might
     * be due to a <code>Simon.release()</code> call, or due to a connection
     * error.
     */
    public void closed();
}
