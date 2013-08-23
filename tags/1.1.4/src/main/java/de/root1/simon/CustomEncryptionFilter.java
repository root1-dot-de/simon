/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.root1.simon;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.filter.util.WriteRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author achristian
 */
public class CustomEncryptionFilter extends WriteRequestFilter {

    private Logger log = LoggerFactory.getLogger(getClass());
    
    /**
     * custom encryption implementation from user
     */
    private CustomEncryption ce;

    CustomEncryptionFilter(CustomEncryption ce) {
        this.ce = ce;
    }

    public CustomEncryption getCustomEncryption() {
        return ce;
    }

    public void setCustomEncryption(CustomEncryption ce) {
        this.ce = ce;
    }
    
    
    @Override
    protected Object doFilterWrite(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
        // dekodieren
        IoBuffer inBuffer = (IoBuffer) writeRequest.getMessage();
        log.info("encrypting {}", inBuffer);
        
        byte[] buf = new byte[inBuffer.limit()];
        inBuffer.get(buf);
        inBuffer.rewind();
        IoBuffer outBuffer = IoBuffer.wrap(ce.encrypt(buf));
        log.info("encrypted \n"
                + "{}\n"
                + "to\n"
                + "{}", inBuffer, outBuffer);
        return outBuffer;
    }
    
    @Override
    public void messageReceived(NextFilter nextFilter, IoSession session, Object message) throws Exception {
        // enkodieren
        IoBuffer inBuffer = (IoBuffer) message;
        log.info("Decrypting {}", inBuffer);
        
        byte[] buf = new byte[inBuffer.limit()];
        inBuffer.get(buf);
        inBuffer.rewind();
        IoBuffer outBuffer = IoBuffer.wrap(ce.decrypt(buf));
        log.info("decrypted \n"
                + "{}\n"
                + "to\n"
                + "{}", inBuffer, outBuffer);
        nextFilter.messageReceived(session, outBuffer); 
    }
    
}
