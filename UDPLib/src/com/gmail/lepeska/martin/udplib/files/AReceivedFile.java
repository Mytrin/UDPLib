package com.gmail.lepeska.martin.udplib.files;

import com.gmail.lepeska.martin.udplib.AGroupThread;
import com.gmail.lepeska.martin.udplib.UDPLibException;
import com.gmail.lepeska.martin.udplib.util.Encryptor;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;


/**
 *
 * SharedFile is client representation of file shared at network.
 * 
 * @author Martin Lepeška
 * 
 * @param <T> type of part data
 */
public abstract class AReceivedFile<T> {
    protected final String name;
    
    protected final AGroupThread client;
    protected final Encryptor encryptor;
    protected final InetAddress source;
    
    private boolean isFinished = false;
    
    private File createdFile;
    /**
     * 
     * @param name Unique name of file
     * @param client Thread with socket, which can SharedFile use for CLIENT_FILE_SHARE_PART_REQUEST
     * @param encryptor object responsible for encrypting requests
     * @param server server ip
     */
    public AReceivedFile(String name, AGroupThread client, Encryptor encryptor, InetAddress server){
        this.name = name;
        this.client = client;
        this.encryptor = encryptor;
        this.source = server;
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
            if(isFinished){
                createFile();
            }
        }else{
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
            client.receiveFile(name, createdFile);
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
    
    /**
     * @param data Data of received part
     * @param checksum control sum received from server
     * @return true, if client calculated checksum is same as server checksum
     */
    public abstract boolean isPartValid(T data, int checksum);
    
}