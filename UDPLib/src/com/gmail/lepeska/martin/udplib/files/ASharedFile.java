package com.gmail.lepeska.martin.udplib.files;

import com.gmail.lepeska.martin.udplib.UDPLibException;
import com.gmail.lepeska.martin.udplib.client.GroupClientThread;
import com.gmail.lepeska.martin.udplib.util.Encryptor;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;


/**
 *
 * SharedFile is client representation of file shared at network.
 * 
 * @author Martin Lepeška
 * 
 * @param <T> type of part data
 */
public abstract class ASharedFile<T> {
    protected final String name;
    
    protected final GroupClientThread client;
    protected final Encryptor encryptor;
    protected final InetAddress server;
    
    private boolean isFinished = false;
    
    private File createdFile;
    /**
     * 
     * @param name Unique name of file
     * @param client Thread with socket, which can SharedFile use for CLIENT_FILE_SHARE_PART_REQUEST
     * @param encryptor object responsible for encrypting requests
     * @param server server ip
     */
    public ASharedFile(String name, GroupClientThread client, Encryptor encryptor, InetAddress server){
        this.name = name;
        this.client = client;
        this.encryptor = encryptor;
        this.server = server;
    }
        
    /**
     * Saves received data to temporary structure, before they are written to file
     * @param index place of part
     * @param content  part data
     */
    public abstract void setPart(int index, T content);
    
    /**
     * Called from client thread when received SERVER_FILE_SHARE_FINISH
     */
    public void finished(){
        if(!isFinished){
            isFinished = finishOrRequest();
            createFile();
        }
    }
    
    /**
     * In case some parts are missing, request server for resending them
     * @return  true, if all parts have been gathered
     */
    protected abstract boolean finishOrRequest();
    
    /**
     * Creates file from complete parts
     */
    private void createFile(){
        try{
            createdFile = File.createTempFile(name, ".tmp"); 
            fillFile(createdFile);
            client.receiveFile(createdFile);
        }catch(Exception e){
            throw new UDPLibException("Cannot create temporary file:", e);
        }
    }
    
    /**
     * Writes received data into shared file
     * @param createdFile file with name received from Server
     * @throws java.io.IOException
     */
    protected abstract void fillFile(File createdFile)throws IOException;
    
}