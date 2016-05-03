package com.gmail.lepeska.martin.udplib.files;

import com.gmail.lepeska.martin.udplib.Datagrams;
import com.gmail.lepeska.martin.udplib.UDPLibException;
import com.gmail.lepeska.martin.udplib.client.GroupClientThread;
import com.gmail.lepeska.martin.udplib.util.Encryptor;
import java.io.File;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 *
 * SharedFile is client representation of file shared at network.
 * 
 * @author Martin Lepeška
 */
public class SharedFile {
    private final String name;
    private final String[] parts;
    private final GroupClientThread client;
    private final Encryptor encryptor;
    
    private final InetAddress server;
    
    private boolean isFinished = false;
    
    private File createdFile;
    /**
     * 
     * @param name Unique name of file
     * @param parts Count of fileParts to receive
     * @param client Thread with socket, which can SharedFile use for CLIENT_FILE_SHARE_PART_REQUEST
     * @param encryptor object responsible for encrypting requests
     * @param server server ip
     */
    public SharedFile(String name, int parts, GroupClientThread client, Encryptor encryptor, InetAddress server){
        this.name = name;
        this.parts = new String[parts];
        this.client = client;
        this.encryptor = encryptor;
        this.server = server;
    }
    
    /**
     * Called from client thread to append file part
     * @param index index of given part in array
     * @param content data
     */
    public void partReceived(int index, String content){
        if(!isFinished){
            parts[index] = content;
        }
    }
    
    /**
     * Called from client thread when received SERVER_FILE_SHARE_FINISH
     */
    public void finished(){
        if(!isFinished){
            for(int i=0; i < parts.length; i++){
                if(parts[i] == null){
                    byte[] datagram = Datagrams.createClientFileSharePartRequest(encryptor, name, i);
                    client.sendDatagram(server, datagram);
                    break;
                }
            }
            isFinished = true;
            createFile();
        }
    }
    
    /**
     * Creates file from complete parts
     */
    private void createFile(){
        try{
            createdFile = File.createTempFile(name, ".tmp"); 
            Files.write(Paths.get(createdFile.getPath()), Arrays.asList(parts));
            client.receiveFile(createdFile);
        }catch(Exception e){
            throw new UDPLibException("Cannot create temporary file:", e);
        }

    }
    
}