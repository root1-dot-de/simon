/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.samples.rawchannel2.client;

import de.root1.simon.RawChannelDataListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileReceiver implements RawChannelDataListener {

    private FileChannel fc;
    private String filename;
    boolean dataReceiving = false;
    private long start;

    public FileReceiver(String filename) {
        System.out.println("New FileReceiver created for file: "+filename);
        this.filename = filename;
        try {
            fc = new FileOutputStream(new File(filename)).getChannel();
        } catch (FileNotFoundException ex) {
            // cannot really occur, because we wanto CREATE the file.
            ex.printStackTrace();
        }
    }



    public void write(ByteBuffer data) {
        if (!dataReceiving) {
            System.out.println("Receiving data");
            dataReceiving = true;
            start = System.currentTimeMillis();
        }
        try {
            fc.write(data);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void close() {
        System.out.println("FileReceiver closed. Please check received file: "+filename);
        System.out.println("Required "+(System.currentTimeMillis()-start)+"ms");
        try {
            fc.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
