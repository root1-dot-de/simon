/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.root1.simon.test;

import de.root1.simon.Simon;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 *
 * @author achristian
 */
public class PortNumberGenerator {

    private static final Logger logger = LoggerFactory.getLogger(PortNumberGenerator.class);
    private static File f;
    private static int port = 0;

    static {
        f = new File("/tmp/SIMON_PORT_NB.tmp");
    }

    public static synchronized int getNextPort() {
        try {
            String number = null;

            if (f.exists()) {
                FileReader fr = new FileReader(f);
                BufferedReader br = new BufferedReader(fr);
                number = br.readLine();
                if (number == null) {
                    number = Simon.DEFAULT_PORT + "";
                }
                fr.close();
            } else {
                number = Simon.DEFAULT_PORT + "";
            }

            port = Integer.parseInt(number);
            if (port>6000) { // limit
                port = Simon.DEFAULT_PORT;
            }
            int p = port;
            logger.info("using next port: {}", p);
            
            port++;
            FileWriter fw = new FileWriter(f);
            fw.write(port + "\r\n");
            fw.close();
            
            return p;
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(PortNumberGenerator.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }

    public static synchronized int getPort() {
        logger.info("using port: {}", port);
        return port;
    }
}
