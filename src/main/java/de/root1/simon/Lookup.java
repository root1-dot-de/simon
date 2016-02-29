/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.root1.simon;

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
    
    /**
     * Sets the address that is used as the source address for the lookup-request.
     * This might be useful if client machine has more than one network interface or server requires a specific client subnet
     * @param sourceAddress 
     */
    void setSourceAddress(InetAddress sourceAddress);

    InetAddress getServerAddress();

    int getServerPort();

    /**
     * Tries to lookup a remote object on the server.
     * A successful lookup includes:
     * <ul>
     * <li>established socket connection to server, if not already connected</li>
     * <li>increased reference counter for this client-to-server connection by 1</li>
     * <li>return of the requested remote object</li>
     * </ul>
     * To avoid leaks, ensure that remote objects are released after use. For releasing objects, please refer to
     * {@link de.root1.simon.Lookup#release(java.lang.Object)}. 
     * 
     * @param lookupString
     * @return the remote object
     * @throws LookupFailedException
     * @throws EstablishConnectionFailed 
     */
    Object lookup(String lookupString) throws LookupFailedException, EstablishConnectionFailed;

    /**
     * Releases are remote object.
     * Releasing a remote objects leads to:
     * <ul>
     * <li>release of related SIMON resources (threads, ...)</li>
     * <li>decrease reference counter for this client-to-server connection by 1</li>
     * <li>if reference counter reaches 0, the client-to-server connection will be disconnected and cleaned up</li>
     * </ul>
     *
     * @param remoteObject
     * @return true, in case of a normal and clean release. false if remoteobject is already released
     * @throws IllegalArgumentException in case of argument is not a releaseable remote object
     */
    boolean release(Object remoteObject);
    
}
