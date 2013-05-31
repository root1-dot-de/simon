/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.root1.simon.filetransmit;

import java.io.File;

/**
 *
 * @author achristian
 */
public interface FileReceiverProgressListener {

    public void started(File f, long length);
    public void inProgress(File f, long bytesReceived, long length);
    public void completed(File f);
    public void aborted(File f, Exception e);
    
}
