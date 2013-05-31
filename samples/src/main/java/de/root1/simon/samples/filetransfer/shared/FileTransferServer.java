/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.root1.simon.samples.filetransfer.shared;

import de.root1.simon.filetransmit.FileReceiver;

public interface FileTransferServer {
    
    public static String BIND_NAME = "FileTransferServer";
    public FileReceiver getFileReceiver();
    
}
