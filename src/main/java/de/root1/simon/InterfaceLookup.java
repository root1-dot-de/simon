/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon;

import de.root1.simon.exceptions.EstablishConnectionFailed;
import de.root1.simon.exceptions.LookupFailedException;
import de.root1.simon.ssl.SslContextFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
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

    public <T> Object lookup(T arg) throws LookupFailedException, EstablishConnectionFailed {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
