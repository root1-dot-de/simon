/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.root1.simon;

import de.root1.simon.codec.messages.MsgNameLookupReturn;
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
 *
 * @author ACHR
 */
public class NameLookup extends AbstractLookup {

    /**
     * The logger used for this class
     */
    private final static Logger logger = LoggerFactory.getLogger(NameLookup.class);
    private final InetAddress serverAddress;
    private final int serverPort;
    private SslContextFactory sslContextFactory;
    private SimonProxyConfig proxyConfig;
    private ClassLoader classLoader;

    protected NameLookup(String host, int port) throws UnknownHostException {
        this(InetAddress.getByName(host), port);
    }

    protected NameLookup(InetAddress serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    @Override
    public Object lookup(String remoteObjectName) throws LookupFailedException, EstablishConnectionFailed {

        logger.debug("begin");

        if (remoteObjectName == null) {
            throw new IllegalArgumentException("Argument cannot be null");
        }

        if (remoteObjectName.length() == 0) {
            throw new IllegalArgumentException("Argument is not a valid remote object name");
        }

        Object proxy = null;

        // check if there is already an dispatcher and key for THIS server
        SessionDispatcherContainer sessionDispatcherContainer = buildSessionDispatcherContainer(remoteObjectName, serverAddress, serverPort, sslContextFactory, proxyConfig);

        Dispatcher dispatcher = sessionDispatcherContainer.getDispatcher();
        IoSession session = sessionDispatcherContainer.getSession();
        /*
         * Create array with interfaces the proxy should have
         * first contact server for lookup of interfaces
         * --> this request blocks!
         */
        MsgNameLookupReturn msg = dispatcher.invokeNameLookup(session, remoteObjectName);

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
                logger.debug("iface: {}", class1.getName());
            }

            /*
             * Creates proxy for method-call-forwarding to server
             */
            SimonProxy handler = new SimonProxy(dispatcher, session, remoteObjectName, listenerInterfaces, true);
            logger.trace("proxy created");

            /*
             * Create the proxy-object with the needed interfaces
             */
            proxy = Proxy.newProxyInstance(SimonClassLoaderHelper.getClassLoader(Simon.class, classLoader), listenerInterfaces, handler);
            logger.debug("end");
            return proxy;

        }
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
}
