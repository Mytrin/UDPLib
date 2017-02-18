package com.gmail.lepeska.martin.udplib.datagrams.explore;

import com.gmail.lepeska.martin.udplib.DatagramTypes;
import com.gmail.lepeska.martin.udplib.datagrams.ADatagram;
import com.gmail.lepeska.martin.udplib.util.Encryptor;
import java.net.DatagramPacket;

/**
 *  Client request for servers to send info about themselves (not encrypted).
 */
public class ExploreRequest extends ADatagram{
    
    public ExploreRequest() {
        super(Encryptor.DUMMY, DatagramTypes.CLIENT_EXPLORE_REQUEST);
        this.data=createEmptyDatagram();
    }
    
    /**
     * Reconstructs UDPLib message datagram from received frame.
     * 
     * @param source received packet containing info about true length of datagram
     */
    public ExploreRequest(DatagramPacket source) {
        super(Encryptor.DUMMY, DatagramTypes.CLIENT_EXPLORE_REQUEST);
        this.data=source.getData();
    }

}
