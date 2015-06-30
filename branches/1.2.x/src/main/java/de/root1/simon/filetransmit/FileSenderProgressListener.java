/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.root1.simon.filetransmit;

import java.io.File;

/**
 * Progress Listener interface for file transfer
 * @author achristian
 */
public interface FileSenderProgressListener {

    /**
     * File-Transfer has been started
     * @param id the ID for that transfer. See also {@link DefaultFileSender#sendFile(java.io.File) }
     * @param f the related file
     * @param length the length in bytes of datan to transfer
     */
    public void started(int id, File f, long length);
    
    /**
     * 
     * @param id the ID for that transfer. See also {@link DefaultFileSender#sendFile(java.io.File) }
     * @param f the related file
     * @param bytesSent
     * @param length 
     */
    public void inProgress(int id, File f, long bytesSent, long length);
    
    /**
     * 
     * @param id the ID for that transfer. See also {@link DefaultFileSender#sendFile(java.io.File) }
     * @param f the related file
     */
    public void completed(int id, File f);
    
    /**
     * 
     * @param id the ID for that transfer. See also {@link DefaultFileSender#sendFile(java.io.File) }
     * @param f the related file
     * @param ex 
     */
    public void aborted(int id, File f, Exception ex);
    
}
