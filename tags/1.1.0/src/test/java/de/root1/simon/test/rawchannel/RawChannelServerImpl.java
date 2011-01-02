/*
 * Copyright (C) 2008 Alexander Christian <alex(at)root1.de>. All rights reserved.
 * 
 * This file is part of SIMON.
 *
 *   SIMON is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SIMON is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with SIMON.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.root1.simon.test.rawchannel;

import de.root1.simon.Simon;
import de.root1.simon.annotation.SimonRemote;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author achristian
 */
@SimonRemote
public class RawChannelServerImpl implements RawChannelServer {

    @Override
    public int openFileChannel(String filename){
        int token = Simon.prepareRawChannel(new FileReceiver(filename),this);
        System.out.println("-> opened raw data channel on server side for file '"+filename+"'. token is: "+token);
        return token;
        
    }

    @Override
    public byte[] getFileBytes(String filename){
        System.out.println("-> transfering received file back to client.");
        File f = new File(filename);

        byte[] data = new byte[(int)f.length()];

        DataInputStream dis;
        try {
            dis = new DataInputStream(new FileInputStream(f));
            dis.readFully(data);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return data;
    }

}
