package com.gmail.lepeska.martin.udplib.datagrams;

import com.gmail.lepeska.martin.udplib.DatagramTypes;
import static com.gmail.lepeska.martin.udplib.datagrams.ADatagram.DELIMITER;
import static com.gmail.lepeska.martin.udplib.datagrams.ADatagram.bytesToString;
import com.gmail.lepeska.martin.udplib.util.Encryptor;
import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * Response from server to new client containing info about NetworkInterfaces.
 * 
 * HEAD(N B)TYPE(1 B)GROUP_IP#ACCEPTED#CLIENT_IP
 */
public class AccessResponseDatagram extends ADatagram{
    
    private final String message;
    
    /**
     * @param encryptor Encryptor of sending thread
     * @param groupAddress Group address of sending thread
     * @param clientAddress IP of client to let him know which interface he should use
     * @param accept true if recipient is accepted to group
     */
    public AccessResponseDatagram(Encryptor encryptor, InetAddress groupAddress, InetAddress clientAddress, boolean accept) {
        super(encryptor, DatagramTypes.SERVER_ACCEPT_CLIENT_RESPONSE);
        this.message = groupAddress.getHostAddress()+DELIMITER+(accept?"1":"0")+DELIMITER+clientAddress.getHostAddress();
        
        this.data= createDatagramDataFromString(message);
    }
    
    /**
     * Reconstructs UDPLib message datagram from received frame.
     * 
     * @param encryptor Encryptor of sending thread
     * @param source received packet containing info about true length of datagram
     */
    public AccessResponseDatagram(Encryptor encryptor, DatagramPacket source) {
        super(encryptor, DatagramTypes.SERVER_IS_ALIVE_REQUEST);
        this.data=source.getData();
        this.message = bytesToString(unpackAndDecrypt(source));
    }

    @Override
    public String[] getStringMessage() {
        return message.split(DELIMITER);
    }
    
}