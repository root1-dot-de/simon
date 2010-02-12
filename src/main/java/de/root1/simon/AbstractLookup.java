/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon;

import de.root1.simon.ClientToServerConnection;
import de.root1.simon.ClosedListener;
import de.root1.simon.Dispatcher;
import de.root1.simon.Simon;
import de.root1.simon.SimonProxy;
import de.root1.simon.SimonProxyConfig;
import de.root1.simon.codec.SimonProxyFilter;
import de.root1.simon.codec.base.SimonProtocolCodecFactory;
import de.root1.simon.exceptions.EstablishConnectionFailed;
import de.root1.simon.exceptions.LookupFailedException;
import de.root1.simon.ssl.SslContextFactory;
import de.root1.simon.utils.FilterEntry;
import de.root1.simon.utils.Utils;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import javax.net.ssl.SSLContext;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.future.CloseFuture;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.OrderedThreadPoolExecutor;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.filter.ssl.SslFilter;
import org.apache.mina.proxy.ProxyConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ACHR
 */
abstract class AbstractLookup implements Lookup {

    /**
     * The logger used for this class
     */
    private final static Logger logger = LoggerFactory.getLogger(AbstractLookup.class);

    /**
     * A relation map between server-connection-string and ClientToServerConnection objects.<br>
     * This is used to re-use already existing connections.<br>
     * This member has static access. So it's reachable from every lookup implementation class
     */
    static final Map<String, ClientToServerConnection> serverDispatcherRelation = new HashMap<String, ClientToServerConnection>();

    static class SessionDispatcherContainer {

        private final Dispatcher dispatcher;
        private final IoSession session;

        public SessionDispatcherContainer(IoSession session, Dispatcher dispatcher) {
            this.session = session;
            this.dispatcher = dispatcher;
        }

        public Dispatcher getDispatcher() {
            return dispatcher;
        }

        public IoSession getSession() {
            return session;
        }
    }

    @Override
    public boolean release(Object proxyObject) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<ClosedListener> getClosedListeners(Object remoteObject) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addClosedListener(Object remoteObject, ClosedListener closedListener) {
        SimonProxy simonProxy = Simon.getSimonProxy(remoteObject);
        Dispatcher dispatcher = simonProxy.getDispatcher();
        dispatcher.addClosedListener(closedListener, simonProxy.getRemoteObjectName());
    }

    @Override
    public boolean removeClosedListener(Object remoteObject, ClosedListener closedListener) {
        SimonProxy simonProxy = Simon.getSimonProxy(remoteObject);
        Dispatcher dispatcher = simonProxy.getDispatcher();
        return dispatcher.removeClosedListener(closedListener, simonProxy.getRemoteObjectName());
    }

    /**
     *
     * Creates a unique string for a server by using the host and port
     *
     * @param host
     *            the servers host
     * @param port
     *            the port the server listens on
     * @return a server string
     */
    String createServerString(InetAddress host, int port) {
        return host.getHostAddress() + ":" + port;
    }

    SessionDispatcherContainer buildSessionDispatcherContainer(String remoteObjectName, InetAddress serverAddress, int serverPort, SslContextFactory sslContextFactory, SimonProxyConfig proxyConfig) throws EstablishConnectionFailed {

        Dispatcher dispatcher = null;
        IoSession session = null;

        String serverString = createServerString(serverAddress, serverPort);

        logger.debug("check if serverstring '{}' is already in the serverDispatcherRelation list", serverString);

        synchronized (serverDispatcherRelation) {

            if (serverDispatcherRelation.containsKey(serverString)) {

                // retrieve the already stored connection
                ClientToServerConnection ctsc = serverDispatcherRelation.remove(serverString);
                ctsc.addRef();
                serverDispatcherRelation.put(serverString, ctsc);
                dispatcher = ctsc.getDispatcher();
                session = ctsc.getSession();
                logger.debug("Got ClientToServerConnection from list");


            } else {

                logger.debug("No ClientToServerConnection in list. Creating new one.");

                dispatcher = new Dispatcher(serverString, Simon.getThreadPool());

                // an executor service for handling the message reading in a threadpool
                ExecutorService filterchainWorkerPool = new OrderedThreadPoolExecutor();

                IoConnector connector = new NioSocketConnector();
                connector.setHandler(dispatcher);

                /* ******************************************
                 * Setup filterchain before connecting to get all events like session created
                 * and session opened within the filters
                 */
                DefaultIoFilterChainBuilder filterChain = connector.getFilterChain();

                // create a list of used filters
                List<FilterEntry> filters = new ArrayList<FilterEntry>();


                // check for SSL
                if (sslContextFactory != null) {
                    SSLContext context = sslContextFactory.getSslContext();

                    if (context != null) {
                        SslFilter sslFilter = new SslFilter(context);
                        sslFilter.setUseClientMode(true); // only on client side needed
                        filters.add(new FilterEntry(sslFilter.getClass().getName(), sslFilter));

                        logger.debug("SSL ON");
                    } else {
                        logger.warn("SSLContext retrieved from SslContextFactory was 'null', so starting WITHOUT SSL!");
                    }
                }

                if (logger.isTraceEnabled()) {
                    filters.add(new FilterEntry(LoggingFilter.class.getName(), new LoggingFilter()));
                }

                // don't use a threading model on filter level
                //filters.add(new FilterEntry(filterchainWorkerPool.getClass().getName(), new ExecutorFilter(filterchainWorkerPool)));

                // add the simon protocol
                SimonProtocolCodecFactory protocolFactory = null;
                try {

                    protocolFactory = Utils.getProtocolFactoryInstance(Simon.getProtocolCodecFactory());

                } catch (ClassNotFoundException e) {
                    logger.error("ClassNotFoundException while preparing ProtocolFactory: {}", e.getMessage());
                    throw new IllegalArgumentException(e);
                } catch (InstantiationException e) {
                    logger.error("InstantiationException while preparing ProtocolFactory: {}", e.getMessage());
                    throw new IllegalArgumentException(e);
                } catch (IllegalAccessException e) {
                    logger.error("IllegalAccessException while preparing ProtocolFactory: {}", e.getMessage());
                    throw new IllegalArgumentException(e);
                }

                protocolFactory.setup(false);
                filters.add(new FilterEntry(protocolFactory.getClass().getName(), new ProtocolCodecFilter(protocolFactory)));

                // setup for proxy connection if necessary
                String connectionTarget;
                if (proxyConfig != null) {

                    // create the proxy filter with reference to the filter list
                    // proxy filter will later on replace all proxy filters etc. with the ones from filter list
                    connectionTarget = proxyConfig.toString();
                    filterChain.addLast(SimonProxyFilter.class.getName(), new SimonProxyFilter(serverAddress.getHostName(), serverPort, proxyConfig, filters));
                    logger.trace("prepared for proxy connection. chain is now: {}", filterChain);

                } else {

                    // add the filters from the list to the filter chain
                    connectionTarget = new StringBuffer().append("Connection[").append(serverAddress).append(":").append(serverPort).append("]").toString();
                    for (FilterEntry relation : filters) {
                        filterChain.addLast(relation.name, relation.filter);
                    }
                }
                logger.debug("Using: {}", connectionTarget);

                // now we can try to connect ...
                ConnectFuture future = null;
                try {

                    // decide whether the connection goes via proxy or not
                    if (proxyConfig == null) {
                        future = connector.connect(new InetSocketAddress(serverAddress, serverPort));
                    } else {
                        future = connector.connect(new InetSocketAddress(proxyConfig.getProxyHost(), proxyConfig.getProxyPort()));
                    }
                    future.awaitUninterruptibly(); // Wait until the connection attempt is finished.


                } catch (Exception e) {

                    if (session != null) {
                        logger.trace("session != null. closing it...");
                        session.close(true);
                    }
                    connector.dispose();
                    dispatcher.shutdown();
                    filterchainWorkerPool.shutdown();

                    throw new EstablishConnectionFailed("Exception occured while connection/getting session for " + connectionTarget + ". Error was: " + e + ": " + e.getMessage());
                }

                if (future.isConnected()) { // check if the connection succeeded

                    session = future.getSession(); // this cannot return null, because we waited uninterruptibly for the connect-process
                    logger.trace("connected with {}. remoteObjectName={}", connectionTarget, remoteObjectName);

                } else {
                    connector.dispose();
                    dispatcher.shutdown();
                    filterchainWorkerPool.shutdown();
                    throw new EstablishConnectionFailed("Could not establish connection to " + connectionTarget + ". Maybe host or network is down?");
                }

                // configure the session
                session.getConfig().setIdleTime(IdleStatus.BOTH_IDLE, Statics.DEFAULT_IDLE_TIME);
                session.getConfig().setWriteTimeout(Statics.DEFAULT_WRITE_TIMEOUT);

                // store this connection for later re-use
                ClientToServerConnection ctsc = new ClientToServerConnection(serverString, dispatcher, session, connector, filterchainWorkerPool);
                ctsc.addRef();
                serverDispatcherRelation.put(serverString, ctsc);
            }
        }

        return new SessionDispatcherContainer(session, dispatcher);
    }

    /**
     *
     * Releases a reference for a {@link Dispatcher} identified by a specific
     * server string (see: {@link Simon#createServerString}. If there is no more
     * server string referencing the Dispatcher, the Dispatcher will be
     * released/shutdown.
     *
     * @param serverString
     *            the identifier of the Dispatcher to release
     * @return true if the Dispatcher is shut down, false if there's still a
     *         reference pending
     */
    protected static boolean releaseServerDispatcherRelation(String serverString) {

        boolean result = false;

        synchronized (serverDispatcherRelation) {

            // if there's an instance of this connection known ...
            if (serverDispatcherRelation.containsKey(serverString)) {

                // ... remove the connection from the list ...
                final ClientToServerConnection ctsc = serverDispatcherRelation.remove(serverString);
                int refCount = ctsc.delRef();

                logger.trace("removed serverString '{}' from serverDispatcherRelation. new refcount is {}", serverString, refCount);

                if (refCount == 0) {
                    // .. and shutdown the dispatcher if there's no further reference
                    logger.debug("refCount reached 0. shutting down session and all related stuff.");
                    ctsc.getDispatcher().shutdown();

                    CloseFuture closeFuture = ctsc.getSession().close(false);

                    closeFuture.addListener(new IoFutureListener<IoFuture>() {

                        public void operationComplete(IoFuture future) {
                            ctsc.getFilterchainWorkerPool().shutdown();
                            ctsc.getConnector().dispose();
                        }
                    });
                    result = true;
                } else {
                    logger.debug("refCount={}. put back the ClientToServerConnection.", refCount);
                    serverDispatcherRelation.put(serverString, ctsc);
                }

            } else {
                logger.debug("no ServerDispatcherRelation found for {}. Maybe remote object is already released?", serverString);
            }

        }
        return result;
    }

    /**
     *  Releases a {@link Dispatcher}. If there is no more
     * server string referencing the Dispatcher, the Dispatcher will be
     * released/shutdown.
     *
     * @param dispatcher
     *            the iDispatcher to release
     * @return true if the Dispatcher is shut down, false if there's still a
     *         reference pending
     */
    protected static boolean releaseDispatcher(Dispatcher dispatcher) {
        // get the serverstring the dispatcher is connected to
        String serverString = dispatcher.getServerString();
        boolean result = releaseServerDispatcherRelation(serverString);
        return result;
    }

}
