/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.samples.rawchannel.server;

import de.root1.simon.RawChannelDataListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileReceiver implements RawChannelDataListener {

    private FileChannel fc;

    public FileReceiver(String filename) {
        try {
            fc = new FileOutputStream(new File(filename)).getChannel();
        } catch (FileNotFoundException ex) {
            // cannot really occur, because we wanto CREATE the file.
            ex.printStackTrace();
        }
    }



    public void write(ByteBuffer data) {
        try {
            fc.write(data);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void close() {
        try {
            fc.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
