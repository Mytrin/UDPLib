package com.gmail.lepeska.martin.udplib.files;

import com.gmail.lepeska.martin.udplib.UDPLibException;
import com.gmail.lepeska.martin.udplib.client.GroupClientThread;
import com.gmail.lepeska.martin.udplib.datagrams.ADatagram;
import com.gmail.lepeska.martin.udplib.datagrams.files.FileSharePartRequest;
import com.gmail.lepeska.martin.udplib.util.Encryptor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 *
 * SharedTextFile is client representation of text file shared at network.
 *
 * @author Martin Lepeška
 */
public class SharedTextFile extends ASharedFile<String> {

    private final String[] parts;

    /**
     *
     * @param name Unique name of file
     * @param parts Count of fileParts to receive
     * @param client Thread with socket, which can SharedFile use for
     * CLIENT_FILE_SHARE_PART_REQUEST
     * @param encryptor object responsible for encrypting requests
     * @param server server ip
     */
    public SharedTextFile(String name, int parts, GroupClientThread client, Encryptor encryptor, InetAddress server) {
        super(name, client, encryptor, server);
        this.parts = new String[parts];
    }
    
    @Override
    public void setPart(int index, String content) {
        parts[index] = content;
    }

    @Override
    protected boolean finishOrRequest() {
        for (int i = 0; i < parts.length; i++) {
            if (parts[i] == null) {
                ADatagram datagram = new FileSharePartRequest(encryptor, name, i);
                client.sendDatagram(server, datagram);
                return false;
            }
        }
        return true;
    }

    @Override
    protected void fillFile(File createdFile) throws IOException {
        Files.write(Paths.get(createdFile.getPath()), Arrays.asList(parts));
        try{
            PrintWriter writer = new PrintWriter(createdFile, ADatagram.ENCODING);
            for (String line : parts) {
                writer.write(line);
            }
            writer.close();
        }catch(FileNotFoundException | UnsupportedEncodingException e){
            throw new UDPLibException("Failed to save text file: ", e);
        }
    }
    
    @Override
    public boolean isPartValid(String data, int checksum) {
        return ServerSharedTextFile.getChecksum(data)==checksum;
    }
    
}