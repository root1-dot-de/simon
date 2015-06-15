/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.root1.simon.test.rawchannel;

import de.root1.simon.RawChannelDataListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author achristian
 */
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



    @Override
    public void write(ByteBuffer data) {
        try {
            System.out.println("   received file data: "+data);
            fc.write(data);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            fc.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
