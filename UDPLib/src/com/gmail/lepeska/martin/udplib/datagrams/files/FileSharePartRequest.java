package com.gmail.lepeska.martin.udplib.datagrams.files;

import com.gmail.lepeska.martin.udplib.DatagramTypes;
import com.gmail.lepeska.martin.udplib.datagrams.ADatagram;
import static com.gmail.lepeska.martin.udplib.datagrams.ADatagram.bytesToString;
import com.gmail.lepeska.martin.udplib.util.Encryptor;
import java.net.DatagramPacket;

/**
 * CLIENT_FILE_SHARE_PART_REQUEST datagram, which client uses to 
 * request resending some parts of shared file it missed.
 * 
 * Format: HEAD(N B)TYPE(1B)FILENAME#INDEX
 */
public class FileSharePartRequest extends ADatagram{
    
    private final String message;
    
    /**
     * @param encryptor  Encryptor of sending thread
     * @param fileName - String, under which should be the SharedFile stored in Map
     * @param index - index of filePart, which is client missing
     * 
     */
    public FileSharePartRequest(Encryptor encryptor, String fileName, int index) {
        super(encryptor, DatagramTypes.CLIENT_FILE_SHARE_PART_REQUEST);
        this.message = fileName+DELIMITER+index;
        this.data=createDatagramDataFromString(message);
    }
    
    /**
     * Reconstructs CLIENT_FILE_SHARE_PART_REQUEST from received packet.
     * 
     * @param encryptor  Encryptor of sending thread
     * @param source received packet containing info about true length of datagram
     */
    public FileSharePartRequest(Encryptor encryptor, DatagramPacket source) {
        super(encryptor, DatagramTypes.CLIENT_FILE_SHARE_PART_REQUEST);
        this.data=source.getData();
        this.message=bytesToString(unpackAndDecrypt(source));
    }

    @Override
    public String[] getStringMessage() {
        return message.split(ADatagram.DELIMITER);
    }
}
