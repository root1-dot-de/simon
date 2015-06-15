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

import de.root1.simon.codec.messages.MsgInterfaceLookupReturn;
import de.root1.simon.exceptions.EstablishConnectionFailed;
import de.root1.simon.exceptions.LookupFailedException;
import de.root1.simon.ssl.SslContextFactory;
import de.root1.simon.utils.SimonClassLoaderHelper;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * With this class, you can make a lookup by providing a Interface.
 * 
 * @author ACHR
 */
public class InterfaceLookup extends AbstractLookup {

    /**
     * The logger used for this class
     */
    private final static Logger logger = LoggerFactory.getLogger(InterfaceLookup.class);
    private final InetAddress serverAddress;
    private final int serverPort;
    private SslContextFactory sslContextFactory;
    private SimonProxyConfig proxyConfig;
    private ClassLoader classLoader;

    protected InterfaceLookup(String host, int port) throws UnknownHostException {
        this(InetAddress.getByName(host), port);
    }

    protected InterfaceLookup(InetAddress serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    @Override
    public SslContextFactory getSslContextFactory() {
        return sslContextFactory;
    }

    @Override
    public void setSslContextFactory(SslContextFactory sslContextFactory) {
        this.sslContextFactory = sslContextFactory;
    }

    @Override
    public SimonProxyConfig getProxyConfig() {
        return proxyConfig;
    }

    @Override
    public void setProxyConfig(SimonProxyConfig proxyConfig) {
        this.proxyConfig = proxyConfig;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public InetAddress getServerAddress() {
        return serverAddress;
    }

    @Override
    public int getServerPort() {
        return serverPort;
    }

    @Override
    public Object lookup(String canonicalInterfaceName) throws LookupFailedException, EstablishConnectionFailed {
        logger.debug("begin");

        if (canonicalInterfaceName == null) {
            throw new IllegalArgumentException("Argument cannot be null");
        }

        if (canonicalInterfaceName.length() == 0) {
            throw new IllegalArgumentException("Argument is not a valid canonical name of remote interface");
        }

        // check if there is already an dispatcher and key for THIS server
        Object proxy = null;

        SessionDispatcherContainer sessionDispatcherContainer = buildSessionDispatcherContainer(canonicalInterfaceName, serverAddress, serverPort, sslContextFactory, proxyConfig);

        Dispatcher dispatcher = sessionDispatcherContainer.getDispatcher();
        IoSession session = sessionDispatcherContainer.getSession();
        /*
         * Create array with interfaces the proxy should have
         * first contact server for lookup of interfaces
         * --> this request blocks!
         */
        MsgInterfaceLookupReturn msg = dispatcher.invokeInterfaceLookup(session, canonicalInterfaceName);

        if (msg.hasError()) {

            logger.trace("Lookup failed. Releasing dispatcher.");
            releaseDispatcher(dispatcher);
            throw new LookupFailedException(msg.getErrorMsg());

        } else {

            Class<?>[] listenerInterfaces=null;
            try {
                listenerInterfaces = (classLoader==null?msg.getInterfaces():msg.getInterfaces(classLoader));
            } catch (ClassNotFoundException ex) {
                throw new LookupFailedException("Not able to load remote interfaces. Maybe you need to specify a specific classloader via Lookup#setClassLoader()?",ex);
            }

            for (Class<?> class1 : listenerInterfaces) {
                logger.trace("iface: {}", class1.getName());
            }

            /*
             * Creates proxy for method-call-forwarding to server
             */
            SimonProxy handler = new SimonProxy(dispatcher, session, msg.getRemoteObjectName(), listenerInterfaces, true);
            logger.trace("proxy created");

            /*
             * Create the proxy-object with the needed interfaces
             */
            proxy = Proxy.newProxyInstance(SimonClassLoaderHelper.getClassLoader(Simon.class, classLoader), listenerInterfaces, handler);
            logger.debug("end");
            return proxy;

        }
    }
}