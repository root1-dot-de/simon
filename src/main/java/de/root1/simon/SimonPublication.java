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

import java.net.*;
import java.util.Collections;
import java.util.Enumeration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class holds a 3-tupel of data needed to identify a remote object on a registry.
 * The tupel consists of:
 * <ol>
 * <li>address to which the registry of the remote object is bind to</li>
 * <li>the port on which the registry listens</li>
 * <li>the remote object name bind to the registry</li>
 * </ol>
 * 
 * @author achristian
 *
 */
public final class SimonPublication {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String SIMON_PUBLISHMENT_HEADER = "[SIMON|";
    private String remoteObjectName;
    private int port;
    private InetAddress address;
    private StringBuffer sb = new StringBuffer();

    /**
     * Creates a new object instance according to the given address, port and remote object name
     * @param address the address of a SIMON registry
     * @param port the port on which the registry listens
     * @param remoteObjectName the bind remote object name
     */
    protected SimonPublication(InetAddress address, int port, String remoteObjectName) {

        if (address.getHostAddress().equalsIgnoreCase("0.0.0.0")) {

            logger.trace("Given IP is 0.0.0.0... Searching for a better one ...");
            boolean ipFound = false;
            Enumeration<NetworkInterface> netInter;
            try {
                netInter = NetworkInterface.getNetworkInterfaces();

                int n = 0;

                while (netInter.hasMoreElements()) {
                    NetworkInterface ni = netInter.nextElement();

                    logger.trace("Analyzing: NetworkInterface #{}: {}", n++, ni.getDisplayName());

                    for (InetAddress iaddress : Collections.list(ni.getInetAddresses())) {

                        if ((iaddress instanceof Inet4Address) && !iaddress.isLoopbackAddress()) {
                            logger.trace("choosing {} instead of 0.0.0.0", iaddress.getHostAddress());
                            address = iaddress;
                            ipFound = true;
                            break;
                        }
                    }
                    if (ipFound) {
                        break;
                    }

                }
                if (!ipFound) {
                    logger.error("no suiteable IP found. Still working with IP 0.0.0.0 which can't be used by clients!");
                }
            } catch (SocketException e) {
                logger.error("Error while detecting IP address. Error was: {}", e.getMessage());
            }
            logger.trace("end with search");
        }

        this.address = address;
        this.port = port;
        this.remoteObjectName = remoteObjectName;
    }

    /**
     * Creates an object instance according to a raw string with the format:<br>
     * <i>[SIMON|192.168.0.123:1234|myServer]</i>
     *
     * @param rawString the string that delivers the needed data to create an instance of {@link SimonPublication}
     * @throws IllegalArgumentException if raw string has the wrong format
     * @throws UnknownHostException if the host in the raw string is unknown
     * @throws NumberFormatException if the port in the raw string has the wrong format (e.g. non numeric)
     */
    protected SimonPublication(String rawString) throws IllegalArgumentException, UnknownHostException, NumberFormatException {

        if ((!rawString.substring(0, SIMON_PUBLISHMENT_HEADER.length()).equals(SIMON_PUBLISHMENT_HEADER)
                && !rawString.substring(SIMON_PUBLISHMENT_HEADER.length() - 1, SIMON_PUBLISHMENT_HEADER.length()).equals("]"))) {
            throw new IllegalArgumentException("provided raw string has the wrong format: " + rawString);
        }

        String values = rawString.substring(SIMON_PUBLISHMENT_HEADER.length(), rawString.length() - 1);

        String[] valuesSplit = values.split("\\|");
        if (valuesSplit.length != 2) {
            throw new IllegalArgumentException("except the header, there must be two values in the raw string. 1st: 'ip:port' 2nd: 'remoteObjectName'. values=" + values);
        }

        String[] addressSplit = valuesSplit[0].split(":");

        if (addressSplit.length != 2) {
            throw new IllegalArgumentException("the address value must contain two values: 1st: 'ip' 2nd: 'port'.");
        }

        address = InetAddress.getByName(addressSplit[0]);
        port = Integer.parseInt(addressSplit[1]);

        if (valuesSplit[1] == null || valuesSplit[1].length() < 1) {
            throw new IllegalArgumentException("'remoteObjectName' must not be 'null' or zero-length.");
        }
        remoteObjectName = valuesSplit[1];
    }

    /**
     * Returns the name of the remote object which is bind to the registry
     * @return name of the remote object
     */
    public String getRemoteObjectName() {
        return remoteObjectName;
    }

    /**
     * Returns the port on which the registry listens for connections
     * @return port on which the registry listens
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns the network address to which the registry is bind
     * @return an address of the registry
     */
    public InetAddress getAddress() {
        return address;
    }

    @Override
    public String toString() {
        sb.delete(0, sb.length());
        sb.append(SIMON_PUBLISHMENT_HEADER);
        sb.append(address.getHostAddress());
        sb.append(":");
        sb.append(port);
        sb.append("|");
        sb.append(remoteObjectName);
        sb.append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof SimonPublication) {

            SimonPublication other = (SimonPublication) obj;

            return (getAddress().equals(other.getAddress())
                    && getPort() == other.getPort()
                    && getRemoteObjectName().equals(other.getRemoteObjectName()));

        } else {
            return false;
        }

    }
}
