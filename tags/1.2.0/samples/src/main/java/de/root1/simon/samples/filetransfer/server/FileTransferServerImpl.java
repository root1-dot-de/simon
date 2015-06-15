package de.root1.simon.samples.filetransfer.server;

import de.root1.simon.filetransmit.DefaultFileReceiver;
import de.root1.simon.Registry;
import de.root1.simon.Simon;
import de.root1.simon.annotation.SimonRemote;
import de.root1.simon.exceptions.NameBindingException;
import de.root1.simon.filetransmit.FileReceiverProgressListener;
import de.root1.simon.samples.filetransfer.shared.FileTransferServer;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SimonRemote(value = {FileTransferServer.class})
public class FileTransferServerImpl implements FileTransferServer {
    
    private final static Logger logger = LoggerFactory.getLogger(FileTransferServerImpl.class);


    DefaultFileReceiver ft;
    
    public FileTransferServerImpl() {
        
        ft = new DefaultFileReceiver();
        ft.setDownloadFolder(new File("/home/achristian/Downloads"));
        
        // optional step, can be skipped if you don't want to track the progress
        ft.addProgressListener(new FileReceiverProgressListener() {

            @Override
            public void started(File f, long length) {
                logger.info("Started receive file: {} size: {}", f, length);
            }

            @Override
            public void inProgress(File f, long bytesReceived, long length) {
                logger.info("In progress receive file: {} received: {}/{}", new Object[]{f, bytesReceived, length});
            }

            @Override
            public void completed(File f) {
                logger.info("Completed receive file: {}", f);
            }

            @Override
            public void aborted(File f, Exception ex) {
                logger.info("Aborted receive file {}: {}", f, ex);
            }
        });
    }

    
    
    @Override
    public DefaultFileReceiver getFileReceiver() {
        return ft;
    }
    
    public static void main(String[] args) throws UnknownHostException, IOException, NameBindingException {
        Registry registry = Simon.createRegistry();
        registry.start();
        registry.bind(FileTransferServer.BIND_NAME, new FileTransferServerImpl());
        System.out.println("Server running");
        
//        registry.stop();
    }
    
}
