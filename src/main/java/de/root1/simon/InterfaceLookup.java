/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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

    public SslContextFactory getSslContextFactory() {
        return sslContextFactory;
    }

    public void setSslContextFactory(SslContextFactory sslContextFactory) {
        this.sslContextFactory = sslContextFactory;
    }

    public SimonProxyConfig getProxyConfig() {
        return proxyConfig;
    }

    public void setProxyConfig(SimonProxyConfig proxyConfig) {
        this.proxyConfig = proxyConfig;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public InetAddress getServerAddress() {
        return serverAddress;
    }

    public int getServerPort() {
        return serverPort;
    }

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

            Class<?>[] listenerInterfaces = msg.getInterfaces();

            for (Class<?> class1 : listenerInterfaces) {
                logger.trace("iface: {}", class1.getName());
            }

            /*
             * Creates proxy for method-call-forwarding to server
             */
            SimonProxy handler = new SimonProxy(dispatcher, session, msg.getRemoteObjectName(), listenerInterfaces);
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