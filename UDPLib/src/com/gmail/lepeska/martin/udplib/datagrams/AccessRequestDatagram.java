package com.gmail.lepeska.martin.udplib.datagrams;

import com.gmail.lepeska.martin.udplib.DatagramTypes;
import com.gmail.lepeska.martin.udplib.util.Encryptor;
import java.net.DatagramPacket;

/**
 * CLIENT_ACCESS_REQUEST datagram, which is used in client's attempt
 * to join group.
 * 
 * Format: HEAD(N B)TYPE(1B)USERNAME|PASSWORD
 */
public class AccessRequestDatagram extends ADatagram{
    
    private final String message;
    
    /**
     * @param encryptor Encryptor of sending thread
     * @param username Client's name in group
     * @param password Group password
     */
    public AccessRequestDatagram(Encryptor encryptor, String username, String password) {
        super(encryptor,DatagramTypes.CLIENT_ACCESS_REQUEST);
        this.message = password+DELIMITER+username;
        
        this.data=createDatagramDataFromString(message);
    }
    
    /**
     * Reconstructs UDPLib message datagram from received frame.
     * 
     * @param encryptor Encryptor of sending thread
     * @param source received packet containing info about true length of datagram
     */
    public AccessRequestDatagram(Encryptor encryptor, DatagramPacket source) {
        super(encryptor, DatagramTypes.CLIENT_ACCESS_REQUEST);
        this.data=source.getData();
        this.message = bytesToString(unpackAndDecrypt(source));
    }

    @Override
    public String[] getStringMessage() {
        return message.split(DELIMITER);
    }

}