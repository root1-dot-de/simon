/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.root1.simon;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ACHR
 */
class NewMonitor extends Monitor {

    private final Semaphore s = new Semaphore(1);

    
    NewMonitor(int sequenceId) {
        super(sequenceId);
        try {
            s.acquire();
        } catch (InterruptedException ex) {
            Logger.getLogger(NewMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public boolean waitForSignal() throws InterruptedException {
        return s.tryAcquire(Statics.MONITOR_WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
    }
    
    public void signal() {
        s.release();
    }
    
}