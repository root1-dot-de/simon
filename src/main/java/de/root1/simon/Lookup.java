/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.root1.simon;

import de.root1.simon.ClosedListener;
import de.root1.simon.SimonProxyConfig;
import de.root1.simon.exceptions.EstablishConnectionFailed;
import de.root1.simon.exceptions.LookupFailedException;
import de.root1.simon.ssl.SslContextFactory;
import java.net.InetAddress;
import java.util.List;

/**
 *
 * @author ACHR
 */
public interface Lookup {

    SslContextFactory getSslContextFactory();

    void setSslContextFactory(SslContextFactory sslContextFactory);

    SimonProxyConfig getProxyConfig();

    void setProxyConfig(SimonProxyConfig proxyConfig);

    /**
     * Returns a list of attached <code>ClosedListener</code>s.
     *
     * @param remoteObject the remote object to query for attached closed listeners
     * @return a list of attached closed listeners
     */
    public List<ClosedListener> getClosedListeners(Object remoteObject);

    /**
     * Attaches a closed listener to the specified remote object
     * @param remoteObject the remote object to which the listener is attached to
     * @param closedListener the listener to add
     */
    public void addClosedListener(Object remoteObject, ClosedListener closedListener);

    /**
     * Removes an already attached closed listener from the specified remote object
     * @param remoteObject the remote object from which the listener has to be removed
     * @param closedListener the listener to remove
     * @return true, if listener was removed, false if there is no listener to remove
     */
    public boolean removeClosedListener(Object remoteObject, ClosedListener closedListener);

    ClassLoader getClassLoader();

    void setClassLoader(ClassLoader classLoader);

    InetAddress getServerAddress();

    int getServerPort();

    Object lookup(String lookupString) throws LookupFailedException, EstablishConnectionFailed;

    /**
     * Releases are remote object. If provided object is null, method will simply return.
     *
     * @param remoteObject
     * @return true, in case of a normal and clean release. false if remoteobject is already released
     */
    boolean release(Object remoteObject);
}
