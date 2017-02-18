package com.gmail.lepeska.martin.udplib.datagrams;

import com.gmail.lepeska.martin.udplib.DatagramTypes;
import com.gmail.lepeska.martin.udplib.client.GroupUser;
import static com.gmail.lepeska.martin.udplib.datagrams.ADatagram.bytesToString;
import com.gmail.lepeska.martin.udplib.util.Encryptor;
import java.net.DatagramPacket;

/**
 * Creates SERVER_CLIENT_DEAD datagram, which server uses to update clients
 * info about disconnected users.
 * 
 * Format: HEADER(N bytes)TYPE(1B)NAME#IP
 */
public class UserDeadDatagram extends ADatagram{
    
    private final String message;
    
    /**
     * @param encryptor  Encryptor of sending thread
     * @param user user, about whom is this info
     */
    public UserDeadDatagram(Encryptor encryptor, GroupUser user) {
        super(encryptor, DatagramTypes.SERVER_CLIENT_DEAD);
        this.message = user.name+DELIMITER+user.ip.getHostAddress();
        this.data=createDatagramDataFromString(message);
    }
    
    /**
     * Reconstructs SERVER_CLIENTS_INFO from received packet.
     * 
     * @param encryptor  Encryptor of sending thread
     * @param source received packet containing info about true length of datagram
     */
    public UserDeadDatagram(Encryptor encryptor, DatagramPacket source) {
        super(encryptor, DatagramTypes.SERVER_CLIENT_DEAD);
        this.data=source.getData();
        this.message=bytesToString(unpackAndDecrypt(source));
    }

    @Override
    public String[] getStringMessage() {
        return message.split(ADatagram.DELIMITER);
    }

}
