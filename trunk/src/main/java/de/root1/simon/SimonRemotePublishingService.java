/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.root1.simon;

import de.root1.simon.exceptions.SimonException;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author achristian
 * @since 1.3.0
 */
interface SimonRemotePublishingService {
    
    public static final String REMOTE_OBJECT_NAME = "REMOTE_OBJECT#"+SimonRemotePublishingService.class.getCanonicalName();

    public UUID publish(SimonPublication publication) throws SimonException ;
    public boolean unpublish(UUID id);
    public List<SimonPublication> searchRemoteObject(String remoteObjectname);
    
}
