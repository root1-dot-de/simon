package de.root1.simon.samples.filetransfer.client;

import de.root1.simon.filetransmit.DefaultFileSender;
import de.root1.simon.filetransmit.FileReceiver;
import de.root1.simon.Lookup;
import de.root1.simon.Simon;
import de.root1.simon.exceptions.EstablishConnectionFailed;
import de.root1.simon.exceptions.LookupFailedException;
import de.root1.simon.filetransmit.FileSenderProgressListener;
import de.root1.simon.samples.filetransfer.shared.FileTransferServer;
import java.io.File;
import java.net.UnknownHostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileTransferClient {
    
    private final static Logger logger = LoggerFactory.getLogger(FileTransferClient.class);
    
    
    public static void main(String[] args) throws UnknownHostException, LookupFailedException, EstablishConnectionFailed {
        Lookup nameLookup = Simon.createNameLookup("localhost");
        FileTransferServer server = (FileTransferServer) nameLookup.lookup(FileTransferServer.BIND_NAME);
        System.out.println("Connected");
        FileReceiver fileReceiver = server.getFileReceiver();
        
        // connect file sender with file receiver
        DefaultFileSender fs = new DefaultFileSender(fileReceiver);
        
        // optional step, can be skipped if you don't want to track the progress
        fs.addProgressListener(new FileSenderProgressListener() {

            @Override
            public void started(int id, File f, long length) {
                logger.info("Started file transfer: {}@{} size: {}", new Object[]{id, f.getName(), length});
            }

            @Override
            public void inProgress(int id, File f, long bytesSent, long length) {
                logger.info("In Progress {}@{} {}/{}", new Object[]{id, f.getName(), bytesSent, length});
            }

            @Override
            public void completed(int id, File f) {
                logger.info("Completed file transfer: {}@{}", new Object[]{id, f.getName()});
            }

            @Override
            public void aborted(int id, File f, Exception ex) {
                logger.info("Aborted file transfer: {}@{}: {}", new Object[]{id, f.getName(), ex});
            }
            
            
        });
        
        System.out.println("Prepared sender. Sending ...");
        fs.sendFile(new File("/home/achristian/Arbeitsfläche/xyz.dat"), true);
        System.out.println("done");
        
        // dont't forget to close, otherwise the send-thread will continue idle'ing around
        fs.close();
        
                
    }
    
}
