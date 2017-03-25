package com.gmail.lepeska.martin.udplib.files;

import com.gmail.lepeska.martin.udplib.AGroupThread;
import com.gmail.lepeska.martin.udplib.datagrams.ADatagram;
import com.gmail.lepeska.martin.udplib.datagrams.files.FileSharePartRequest;
import com.gmail.lepeska.martin.udplib.util.Encryptor;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;

/**
 *
 * SharedBinaryFile is client representation of image file shared at network.
 *
 * @author Martin Lepeška
 */
public class ReceivedBinaryFile extends AReceivedFile<byte[]> {

    private final byte[] data;
    private byte[] lastPart; //last part has not fixed length
    private final boolean[] receivedBlocks;

    /**
     * @param name Unique name of file
     * @param parts Count of fileParts to receive
     * @param client Thread with socket, which can SharedFile use for
     * CLIENT_FILE_SHARE_PART_REQUEST
     * @param encryptor object responsible for encrypting requests
     * @param server server ip
     */
    public ReceivedBinaryFile(String name, int parts, AGroupThread client, Encryptor encryptor, InetAddress server) {
        super(name, client, encryptor, server);
        this.data = new byte[SharedBinaryFile.DATA_LENGTH*parts];
        this.receivedBlocks = new boolean[parts+1];
    }

    @Override
    public void setPart(int index, byte[] content) {
        if(index+1 != receivedBlocks.length){
            System.arraycopy(content, 0, data, index*SharedBinaryFile.DATA_LENGTH, content.length);
        }else{
            lastPart = content;
        }
        
        receivedBlocks[index] = true;
    }

    @Override
    protected boolean finishOrRequest() {
        for (int i = 0; i < receivedBlocks.length; i++) {
            if (receivedBlocks[i] == false) {
                ADatagram datagram = new FileSharePartRequest(encryptor, name, i);
                client.sendDatagram(source, datagram);
                return false;
            }
        }
        return true;
    }

    @Override
    protected void fillFile(File createdFile) throws IOException {
        //memory is faster than hdd
        byte[] completeData = new byte[data.length+lastPart.length];
        System.arraycopy(data, 0, completeData, 0, data.length);
        System.arraycopy(lastPart, 0, completeData, data.length, lastPart.length);
        
        Files.write(createdFile.toPath(), completeData);
    }

    @Override
    public boolean isPartValid(byte[] data, int checksum) {
        return SharedBinaryFile.getChecksum(data)==checksum;
    }
    
}