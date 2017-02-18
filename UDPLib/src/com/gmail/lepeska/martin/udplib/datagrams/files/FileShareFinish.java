package com.gmail.lepeska.martin.udplib.datagrams.files;

import com.gmail.lepeska.martin.udplib.DatagramTypes;
import com.gmail.lepeska.martin.udplib.datagrams.ADatagram;
import static com.gmail.lepeska.martin.udplib.datagrams.ADatagram.bytesToString;
import com.gmail.lepeska.martin.udplib.util.Encryptor;
import java.net.DatagramPacket;

/**
 * Creates SERVER_FILE_SHARE_FINISH datagram, which server uses to inform users
 * that all file parts have been sent.
 * 
 * Format: HEADER(N bytes)TYPE(1B)NAME
 */
public class FileShareFinish extends ADatagram{
    
    private final String message;
    
    /**
     * @param encryptor  Encryptor of sending thread
     * @param fileName - String, under which should be the SharedFile stored in Map
     */
    public FileShareFinish(Encryptor encryptor, String fileName) {
        super(encryptor, DatagramTypes.SERVER_FILE_SHARE_FINISH);
        this.message = fileName;
        this.data=createDatagramDataFromString(message);
    }
    
    /**
     * Reconstructs SERVER_FILE_SHARE_FINISH from received packet.
     * 
     * @param encryptor  Encryptor of sending thread
     * @param source received packet containing info about true length of datagram
     */
    public FileShareFinish(Encryptor encryptor, DatagramPacket source) {
        super(encryptor, DatagramTypes.SERVER_FILE_SHARE_FINISH);
        this.data=source.getData();
        this.message=bytesToString(unpackAndDecrypt(source));
    }

    @Override
    public String[] getStringMessage() {
        return message.split(ADatagram.DELIMITER);
    }
}
