package com.gmail.lepeska.martin.udplib.datagrams;

import com.gmail.lepeska.martin.udplib.DatagramTypes;
import com.gmail.lepeska.martin.udplib.util.Encryptor;
import java.net.DatagramPacket;

/**
 * UDPLib message datagram.
 */
public class MessageDatagram extends ADatagram{

    private final String message;
    
    /**
     * @param encryptor  Encryptor of sending thread
     * @param message data to be sent
     * @param isMulticast true if message is to be sent to all group users
     * @param isServer role of sender
     */
    public MessageDatagram(Encryptor encryptor, String message, boolean isMulticast, boolean isServer) {
        super(encryptor, isServer //TODO this is evil, but better then static or other class
                ?(isMulticast?DatagramTypes.SERVER_MULTICAST_MESSAGE:DatagramTypes.SERVER_UNICAST_MESSAGE)
                :(isMulticast?DatagramTypes.CLIENT_MULTICAST_MESSAGE:DatagramTypes.CLIENT_UNICAST_MESSAGE));
        this.message=message;
        this.data=createDatagramDataFromString(message);
    }

    /**
     * Reconstructs UDPLib message from received frame.
     * 
     * @param encryptor Encryptor of sending thread
     * @param source received packet containing info about true length of datagram
     */
    public MessageDatagram(Encryptor encryptor, DatagramPacket source) {
        super(encryptor, ADatagram.getDatagramType(source.getData()));
        this.data=source.getData();
        this.message=bytesToString(unpackAndDecrypt(source));
    }

    @Override
    public String[] getStringMessage() {
        return new String[]{message};
    }
        
}
