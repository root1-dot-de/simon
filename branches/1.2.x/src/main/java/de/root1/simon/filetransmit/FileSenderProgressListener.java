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
public interface FileSenderProgressListener {

    public void started(int id, File f, long length);
    public void inProgress(int id, File f, long bytesSent, long length);
    public void completed(int id, File f);
    public void aborted(int id, File f, Exception ex);
    
}
