package com.gmail.lepeska.martin.udplib.datagrams;

import com.gmail.lepeska.martin.udplib.DatagramTypes;
import com.gmail.lepeska.martin.udplib.util.Encryptor;
import java.net.DatagramPacket;


/**
 * Creates UDPLib IS_ALIVE request/response datagram data.
 */
public class IsAliveDatagram extends ADatagram{
    
    /**
     * @param encryptor Encryptor of sending thread
     * @param isRequest true if server
     */
    public IsAliveDatagram(Encryptor encryptor, boolean isRequest) {
        super(encryptor, isRequest?
                DatagramTypes.SERVER_IS_ALIVE_REQUEST:DatagramTypes.CLIENT_IS_ALIVE_RESPONSE);
        this.data=createEmptyDatagram();
    }
    
    /**
     * Reconstructs UDPLib message datagram from received frame.
     * 
     * @param encryptor Encryptor of sending thread
     * @param source received packet containing info about true length of datagram
     */
    public IsAliveDatagram(Encryptor encryptor, DatagramPacket source) {
        super(encryptor, Datagrams.getDatagramType(source));
        this.data=source.getData();
    }

}
