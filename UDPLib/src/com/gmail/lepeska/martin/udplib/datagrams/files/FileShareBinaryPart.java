package com.gmail.lepeska.martin.udplib.datagrams.files;

import com.gmail.lepeska.martin.udplib.DatagramTypes;
import com.gmail.lepeska.martin.udplib.datagrams.ADatagram;
import static com.gmail.lepeska.martin.udplib.datagrams.ADatagram.bytesToString;
import com.gmail.lepeska.martin.udplib.files.SharedBinaryFile;
import com.gmail.lepeska.martin.udplib.util.Encryptor;
import java.net.DatagramPacket;
import java.util.Arrays;
import java.util.logging.Logger;


/**
 * Creates BINARY_FILE_SHARE_PART datagram, which contains indexed part
 * of file shared by server.
 * 
 * Format: HEAD(N B)TYPE(1B)FILENAME#INDEX#TOTAL#CHECKSUM#(EMPTY HEADER BYTES)(DATA)(EMPTY DATA BYTES)
 * 
 */
public class FileShareBinaryPart extends ADatagram {
    
    private final String header;
    private final byte[] filePart;
    
    /**
     * @param encryptor  Encryptor of sending thread
     * @param fileName - String, under which should be the SharedFile stored in Map
     * @param filePart - Data of file part to send
     * @param index - index of filePart, which is client missing
     * @param total - count of all parts, which were or will be sent, so client can initialize array of SharedFile
     */
    public FileShareBinaryPart(Encryptor encryptor, String fileName, byte[] filePart, int index, int total) {
        super(encryptor, DatagramTypes.BINARY_FILE_SHARE_PART);
        this.header = fileName+DELIMITER+index+DELIMITER+total
                +DELIMITER+SharedBinaryFile.getChecksum(filePart);
        this.filePart = filePart;
        
        this.data=createDatagramDataFromStringAndBytes(header, filePart);
    }
    
    /**
     * Reconstructs SERVER_BINARY_FILE_SHARE_PART from received packet.
     * 
     * @param encryptor  Encryptor of sending thread
     * @param source received packet containing info about true length of datagram
     */
    public FileShareBinaryPart(Encryptor encryptor, DatagramPacket source) {
        super(encryptor, DatagramTypes.BINARY_FILE_SHARE_PART);
        this.data=source.getData();
        
        byte[] unpacked = unpackAndDecrypt(source);
        byte[] headerPart = Arrays.copyOfRange(unpacked, 0, HEADER_SIZE-1);
        
        this.header = bytesToString(headerPart);
        int copyLength;
        try{
            copyLength = Integer.parseInt(header.split(ADatagram.DELIMITER)[3]);
        }catch(NumberFormatException e){
            Logger.getLogger(FileShareBinaryPart.class.getName()).severe("Corrupted FileShareBinaryPart checksum!");
            copyLength = unpacked.length;
        }
        this.filePart = Arrays.copyOfRange(unpacked, HEADER_SIZE, HEADER_SIZE+copyLength);
    }

    @Override
    public String[] getStringMessage() {
        return header.split(ADatagram.DELIMITER);
    }

    @Override
    public byte[] getBinaryMessage() {
        return filePart;
    }
    
}
