/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.root1.simon;

import de.root1.simon.annotation.SimonRemote;
import de.root1.simon.exceptions.EstablishConnectionFailed;
import de.root1.simon.exceptions.LookupFailedException;
import de.root1.simon.exceptions.NameBindingException;
import de.root1.simon.exceptions.SimonException;
import de.root1.simon.exceptions.SimonRemoteException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.slf4j.LoggerFactory;

/**
 * Internally used by SIMON to provide a Remote Publish Service (publish a local
 * remote object on another registry)
 *
 * @author achristian
 * @since 1.3.0
 */
@SimonRemote(value = SimonRemotePublishingService.class)
public class SimonRemotePublishingServiceImpl implements SimonRemotePublishingService {

    /**
     * local logger
     */
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<UUID, SimonPublication> remotePublications = new HashMap<>();

    SimonRemotePublishingServiceImpl() {
    }

    @Override
    public UUID publish(SimonPublication publication) throws SimonException {
        UUID id = UUID.randomUUID();
        synchronized (remotePublications) {
            if (remotePublications.containsValue(publication)) {
                throw new SimonException(publication + " is already published");
            }
            remotePublications.put(id, publication);
            return id;
        }

    }

    @Override
    public boolean unpublish(UUID id) {
        synchronized (remotePublications) {
            return remotePublications.remove(id) != null;
        }
    }

    @Override
    public List<SimonPublication> searchRemoteObject(String remoteObjectname) {
        List<SimonPublication> result = new ArrayList<>();
        synchronized (remotePublications) {
            for (SimonPublication publication : remotePublications.values()) {
                if (publication.getRemoteObjectName().equals(remoteObjectname)) {

                    // check connectivity
                    try {
                        Lookup lu = Simon.createNameLookup(publication.getAddress(), publication.getPort());
                        Object o = lu.lookup(publication.getRemoteObjectName());
                        lu.release(o);
                        result.add(publication);
                    } catch (LookupFailedException | EstablishConnectionFailed ex) {
                        logger.warn("{} is no longer reachable. Removing it from remote oublication service list", publication);
                    }

                }

            }
        }
        return result;
    }

    public void shutdownService() {
        remotePublications.clear();
    }

}
