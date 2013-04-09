package de.root1.simon.samples.rawchannelstream.client;

import de.root1.simon.RawChannelInputStream;
import de.root1.simon.Simon;
import de.root1.simon.annotation.SimonRemote;
import de.root1.simon.samples.rawchannelstream.shared.ClientCallback;
import java.io.IOException;

@SimonRemote(value={ClientCallback.class})
public class ClientCallbackImpl implements ClientCallback {

    private RawChannelInputStream rcis;

    public ClientCallbackImpl() throws IOException {
        rcis = new RawChannelInputStream();
        rxThread.start();
    }
    
    Thread rxThread = new Thread(){

        @Override
        public void run() {
            String message = "";
            byte[] data = new byte[8*1024];
            int read=0;
            try {
                
                while ((read=rcis.read(data))!=-1) {
                    
                    message += new String(data, 0, read);
                }
                rcis.close();
                System.out.println("Received message: " +message);
                
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
      
    };
    
    // register the file receiver and return the channel token
    @Override
    public int establishStream() {
        return Simon.prepareRawChannel(rcis.getRawChannelDataListener(), this);
    }

}
