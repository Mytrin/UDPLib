package com.gmail.lepeska.martin.udplib.files;

import com.gmail.lepeska.martin.udplib.Datagrams;
import com.gmail.lepeska.martin.udplib.client.GroupClientThread;
import com.gmail.lepeska.martin.udplib.util.ConfigLoader;
import com.gmail.lepeska.martin.udplib.util.Encryptor;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.file.Files;

/**
 *
 * SharedBinaryFile is client representation of image file shared at network.
 *
 * @author Martin Lepeška
 */
public class SharedBinaryFile extends ASharedFile<byte[]> {

    private final byte[] data;
    private final boolean[] receivedBlocks;
    
    /**
     * @param name Unique name of file
     * @param parts Count of fileParts to receive
     * @param client Thread with socket, which can SharedFile use for
     * CLIENT_FILE_SHARE_PART_REQUEST
     * @param encryptor object responsible for encrypting requests
     * @param server server ip
     */
    public SharedBinaryFile(String name, int parts, GroupClientThread client, Encryptor encryptor, InetAddress server) {
        super(name, client, encryptor, server);
        this.data = new byte[Datagrams.MAXIMUM_DATA_LENGTH*parts];
        this.receivedBlocks = new boolean[parts];
    }

    @Override
    public void setPart(int index, byte[] content) {
        System.arraycopy(content, 0, data, index*Datagrams.MAXIMUM_DATA_LENGTH, content.length);
        receivedBlocks[index] = true;
    }

    @Override
    protected boolean finishOrRequest() {
        for (int i = 0; i < receivedBlocks.length; i++) {
            if (receivedBlocks[i] == false) {
                System.out.println("Missing part: "+i);
                byte[] datagram = Datagrams.createClientFileSharePartRequest(encryptor, name, i);
                client.sendDatagram(server, datagram);
                return false;
            }
        }
        return true;
    }

    @Override
    protected void fillFile(File createdFile) throws IOException {
        Files.write(createdFile.toPath(), data);
    }
}