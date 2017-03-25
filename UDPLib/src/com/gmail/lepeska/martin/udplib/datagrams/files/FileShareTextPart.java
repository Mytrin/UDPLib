package com.gmail.lepeska.martin.udplib.datagrams.files;

import com.gmail.lepeska.martin.udplib.DatagramTypes;
import com.gmail.lepeska.martin.udplib.datagrams.ADatagram;
import com.gmail.lepeska.martin.udplib.files.SharedTextFile;
import com.gmail.lepeska.martin.udplib.util.Encryptor;
import java.net.DatagramPacket;

/**
 * Creates TEXT_FILE_SHARE_PART datagram, which contains indexed part
 * of file shared by server.
 * 
 * Format: HEAD(N B)TYPE(1B)FILENAME#INDEX#TOTAL#CHECKSUM#PART
 * 
 */
public class FileShareTextPart extends ADatagram{
    private static final int MESSAGE_INDEX = 5;
    private final String message;
    
    /**
     * @param encryptor  Encryptor of sending thread
     * @param fileName - String, under which should be the SharedFile stored in Map
     * @param filePart - Line or part of line of file to send
     * @param index - index of filePart, which is client missing
     * @param total - count of all parts, which were or will be sent, so client can initialize array of SharedFile
     * 
     */
    public FileShareTextPart(Encryptor encryptor, String fileName, String filePart, int index, int total) {
        super(encryptor, DatagramTypes.TEXT_FILE_SHARE_PART);
        this.message = fileName+DELIMITER+index+DELIMITER+total+DELIMITER
                +SharedTextFile.getChecksum(filePart)+DELIMITER+filePart;
        this.data=createDatagramDataFromString(message);
    }
    
    /**
     * Reconstructs SERVER_FILE_SHARE_PART from received packet.
     * 
     * @param encryptor  Encryptor of sending thread
     * @param source received packet containing info about true length of datagram
     */
    public FileShareTextPart(Encryptor encryptor, DatagramPacket source) {
        super(encryptor, DatagramTypes.TEXT_FILE_SHARE_PART);
        this.data=source.getData();
        this.message=bytesToString(unpackAndDecrypt(source));
    }

    @Override
    public String[] getStringMessage() {
        return message.split(ADatagram.DELIMITER, MESSAGE_INDEX);
    }
}