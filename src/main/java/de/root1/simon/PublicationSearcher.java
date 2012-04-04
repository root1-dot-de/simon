/*
 * Copyright (c) 1995 - 2008 Sun Microsystems, Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.root1.simon;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO document me
 * @author achr
 *
 */
public class PublicationSearcher extends Thread {

    private static final int groupPort = 4446;
    private InetAddress groupAddress = InetAddress.getByName("239.1.2.3");
    private long searchTime = 2000;
    private final List<SimonPublication> foundPublications;
    private boolean shutdown = false;
    private int searchProgress = 0;
    private List<SearchProgressListener> listeners = new ArrayList<SearchProgressListener>();

    /**
     * Creates a searcher instance that searches for published remote objects on
     * the local network
     *
     * @param listener
     *            a {@link SearchProgressListener} implementation which is
     *            informed about the current search progress
     * @param searchTime
     *            the time the background search thread spends for searching
     *            published remote objects
     * @throws IOException
     */
    protected PublicationSearcher(SearchProgressListener listener, int searchTime) throws IOException {
        setName(Statics.PUBLISH_CLIENT_THREAD_NAME);
        foundPublications = new ArrayList<SimonPublication>();
        addSearchProgressListener(listener);
        this.searchTime = searchTime;
    }

    @Override
    public void run() {
        DatagramSocket socket;
        try {
            socket = new DatagramSocket(groupPort - 1);

            byte[] requestData = Statics.REQUEST_STRING.getBytes();
            DatagramPacket searchPacket = new DatagramPacket(requestData, requestData.length, groupAddress, groupPort);
            socket.send(searchPacket);
            socket.setSoTimeout(Statics.DEFAULT_SOCKET_TIMEOUT); // set socket timeout to 100ms

            DatagramPacket packet;

            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() < (startTime + searchTime) && !shutdown) {

                try {
                    byte[] buf = new byte[256];
                    packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);
                    String received = new String(packet.getData(), 0, packet.getLength());
                    synchronized (foundPublications) {
                        foundPublications.add(new SimonPublication(received));
                    }

                } catch (SocketTimeoutException e) {
                    // do nothing
                }

                searchProgress = (int) (100d / searchTime * (System.currentTimeMillis() - startTime));
                if (searchProgress > 100) {
                    searchProgress = 100;
                }
                updateListeners();

            }
            if (searchProgress != 100) {
                searchProgress = 100;
                updateListeners();
            }
            listeners.clear();
            socket.close();

        } catch (SocketException e1) {
            // TODO react on exception
            e1.printStackTrace();
        } catch (IOException e) {
            // TODO react on exception
            e.printStackTrace();
        }
    }

    /**
     * Signals a shutdown request to the search thread.<br>
     * <i><b>Note:</b>This method does not block until shutdown is finished. It returns immediately.</i>
     */
    public void signalShutdown() {
        shutdown = true;
    }

    /**
     * Signals a shutdown to the search thread and waits until the shutdown is processed completely.
     * <i><b>Note:</b>This method blocks until shutdown is finished!</i>
     */
    public void shutdown() {
        signalShutdown();
        while (isAlive()) {
            try {
                Thread.sleep(Statics.WAIT_FOR_SHUTDOWN_SLEEPTIME);
            } catch (InterruptedException e) {
                // do nothing
            }
        }
    }

    /**
     * Returns new found publications
     * @return  found publications since the last call of {@link PublicationSearcher#getNewPublications}
     */
    public List<SimonPublication> getNewPublications() {
        List<SimonPublication> result;
        synchronized (foundPublications) {
            result = new ArrayList<SimonPublication>(foundPublications);
        }
        foundPublications.clear();
        return result;
    }

    /**
     * Returns a value from 0..100 indicating the search progress. 0 is at beginning, 100 at end.
     * @return value 0..100
     */
    public int getSearchProgress() {
        return searchProgress;
    }

    /**
     * Adds a {@link SearchProgressListener} to this publication searcher.
     * Each listener is notified about the current search progress.
     * @param listener the listener to add
     */
    private void addSearchProgressListener(SearchProgressListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    private void updateListeners() {
        int numberOfObjects = 0;
        synchronized (foundPublications) {
            numberOfObjects = foundPublications.size();
        }
        for (SearchProgressListener listener : listeners) {
            listener.update(searchProgress, numberOfObjects);
        }
    }

    /**
     * Returns if thread is still busy with searching
     * @return true if search is in progress, false if search has finished.
     */
    public boolean isSearching() {
        return isAlive();
    }
}
