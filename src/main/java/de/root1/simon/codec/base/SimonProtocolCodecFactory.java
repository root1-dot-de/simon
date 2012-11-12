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
package de.root1.simon.codec.base;
import de.root1.simon.codec.messages.MsgCloseRawChannel;
import de.root1.simon.codec.messages.MsgCloseRawChannelReturn;
import de.root1.simon.codec.messages.MsgEquals;
import de.root1.simon.codec.messages.MsgEqualsReturn;
import de.root1.simon.codec.messages.MsgError;
import de.root1.simon.codec.messages.MsgHashCode;
import de.root1.simon.codec.messages.MsgHashCodeReturn;
import de.root1.simon.codec.messages.MsgInterfaceLookup;
import de.root1.simon.codec.messages.MsgInterfaceLookupReturn;
import de.root1.simon.codec.messages.MsgInvoke;
import de.root1.simon.codec.messages.MsgInvokeReturn;
import de.root1.simon.codec.messages.MsgNameLookup;
import de.root1.simon.codec.messages.MsgNameLookupReturn;
import de.root1.simon.codec.messages.MsgOpenRawChannel;
import de.root1.simon.codec.messages.MsgOpenRawChannelReturn;
import de.root1.simon.codec.messages.MsgPing;
import de.root1.simon.codec.messages.MsgPong;
import de.root1.simon.codec.messages.MsgRawChannelData;
import de.root1.simon.codec.messages.MsgRawChannelDataReturn;
import de.root1.simon.codec.messages.MsgReleaseRef;
import de.root1.simon.codec.messages.MsgToString;
import de.root1.simon.codec.messages.MsgToStringReturn;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.demux.DemuxingProtocolCodecFactory;

/**
 * A {@link ProtocolCodecFactory} that provides a protocol codec for
 * Simon Standard protocol. If one wants to create his own protocol, the new factory
 * has to extend this method and override the setup() method explicitly!
 *
 * @author ACHR
 */
public class SimonProtocolCodecFactory extends DemuxingProtocolCodecFactory {

	/**
	 * Sets up the factory, either in server, or in client mode
	 * @param isServer if true, setup for server mode, false for client mode
	 */
	public void setup(boolean isServer){
            if (isServer) { // **** SERVER ****

                // incoming service lookup
                super.addMessageDecoder(MsgNameLookupDecoder.class);
                // outgoing service lookup return
                super.addMessageEncoder(MsgNameLookupReturn.class, MsgNameLookupReturnEncoder.class);
                
                // incoming interface lookup
                super.addMessageDecoder(MsgInterfaceLookupDecoder.class);
                // outgoing interface lookup return
                super.addMessageEncoder(MsgInterfaceLookupReturn.class, MsgInterfaceLookupReturnEncoder.class);

            }
            else // **** CLIENT ****
            {
                    // outgoing service lookup
                    super.addMessageEncoder(MsgNameLookup.class, MsgNameLookupEncoder.class);
                    // incoming service lookup return
                    super.addMessageDecoder(MsgNameLookupReturnDecoder.class);

                    // outgoing interface lookup
                    super.addMessageEncoder(MsgInterfaceLookup.class, MsgInterfaceLookupEncoder.class);
                    // incoming interface lookup return
                    super.addMessageDecoder(MsgInterfaceLookupReturnDecoder.class);
            }
        
            /* *****************************************
             *  Encoder and Decoder for both sides
             * *****************************************/

            /*
             * invocation handling
             */

            // outgoing invoke
            super.addMessageEncoder(MsgInvoke.class, MsgInvokeEncoder.class);
            // incoming invoke return
            super.addMessageDecoder(MsgInvokeReturnDecoder.class);

            // incoming invoke
            super.addMessageDecoder(MsgInvokeDecoder.class);
            // outgoing invoke return
            super.addMessageEncoder(MsgInvokeReturn.class, MsgInvokeReturnEncoder.class);

            /*
             * "toString()" handling
             */

            // outgoing toString
            super.addMessageEncoder(MsgToString.class, MsgToStringEncoder.class);
            // incoming toString return
            super.addMessageDecoder(MsgToStringReturnDecoder.class);

            // incoming toString
            super.addMessageDecoder(MsgToStringDecoder.class);
            // outgoing toString return
            super.addMessageEncoder(MsgToStringReturn.class, MsgToStringReturnEncoder.class);

            /*
             * "hashCode()" handling
             */

            // outgoing hashCode
            super.addMessageEncoder(MsgHashCode.class, MsgHashCodeEncoder.class);
            // incoming hashCode return
            super.addMessageDecoder(MsgHashCodeReturnDecoder.class);

            // incoming hashCode
            super.addMessageDecoder(MsgHashCodeDecoder.class);
            // outgoing hashCode return
            super.addMessageEncoder(MsgHashCodeReturn.class, MsgHashCodeReturnEncoder.class);

            /*
             * "equals()" handling
             */

            // outgoing equals
            super.addMessageEncoder(MsgEquals.class, MsgEqualsEncoder.class);
            // incoming equals return
            super.addMessageDecoder(MsgEqualsReturnDecoder.class);

            // incoming equals
            super.addMessageDecoder(MsgEqualsDecoder.class);
            // outgoing equals return
            super.addMessageEncoder(MsgEqualsReturn.class, MsgEqualsReturnEncoder.class);


            /*
             * open raw channel handling
             */

            // outgoing open channel
            super.addMessageEncoder(MsgOpenRawChannel.class, MsgOpenRawChannelEncoder.class);
            // incoming open channel return
            super.addMessageDecoder(MsgOpenRawChannelReturnDecoder.class);

            // incoming open channel
            super.addMessageDecoder(MsgOpenRawChannelDecoder.class);
            // outgoing open channel return
            super.addMessageEncoder(MsgOpenRawChannelReturn.class, MsgOpenRawChannelReturnEncoder.class);

            /*
             * close raw channel handling
             */

            // outgoing close channel
            super.addMessageEncoder(MsgCloseRawChannel.class, MsgCloseRawChannelEncoder.class);
            // incoming close channel return
            super.addMessageDecoder(MsgCloseRawChannelReturnDecoder.class);

            // incoming close channel
            super.addMessageDecoder(MsgCloseRawChannelDecoder.class);
            // outgoing close channel return
            super.addMessageEncoder(MsgCloseRawChannelReturn.class, MsgCloseRawChannelReturnEncoder.class);

            /*
             * raw channel data handling
             */

            // outgoing channel data
            super.addMessageEncoder(MsgRawChannelData.class, MsgRawChannelDataEncoder.class);
            // incoming channel data
            super.addMessageDecoder(MsgRawChannelDataDecoder.class);

            // outgoing channel data return
            super.addMessageEncoder(MsgRawChannelDataReturn.class, MsgRawChannelDataReturnEncoder.class);
            // incoming channel data return
            super.addMessageDecoder(MsgRawChannelDataReturnDecoder.class);

            /*
             * ping/pong handling
             */

            // outgoing ping
            super.addMessageEncoder(MsgPing.class, MsgPingEncoder.class);
            // incoming ping
            super.addMessageDecoder(MsgPingDecoder.class);

            // outgoing pong
            super.addMessageEncoder(MsgPong.class, MsgPongEncoder.class);
            // incoming pong
            super.addMessageDecoder(MsgPongDecoder.class);
            
            
            /*
             * error message handling
             */

            // outgoing error
            super.addMessageEncoder(MsgError.class, MsgErrorEncoder.class);
            // incoming error
            super.addMessageDecoder(MsgErrorDecoder.class);
            
            /*
             * DGC handling
             */
            
            // outgoing release ref
            super.addMessageEncoder(MsgReleaseRef.class, MsgReleaseRefEncoder.class);
            // incoming release ref
            super.addMessageDecoder(MsgReleaseRefDecoder.class);
            

	}
}

