package com.gmail.lepeska.martin.udplib.files;

import com.gmail.lepeska.martin.udplib.AGroupThread;
import com.gmail.lepeska.martin.udplib.datagrams.Datagrams;
import com.gmail.lepeska.martin.udplib.util.ConfigLoader;
import com.gmail.lepeska.martin.udplib.util.Encryptor;
import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contains common file sharing logic
 */
public class FileSharing {

   private final HashMap<String, ASharedFile> sharedFiles = new HashMap<>();
   private final HashMap<String, AReceivedFile> receivedFiles = new HashMap<>();

   private final AGroupThread shareThread;
   private final Encryptor encryptor;
   
   /**
    * @param shareThread Class responsible for sending datagrams
    * @param encryptor  Class responsible for encrypting and decrypting file parts
    */
   public FileSharing(AGroupThread shareThread, Encryptor encryptor){
       this.shareThread = shareThread;
       this.encryptor = encryptor;
   }
   
    /**
     * @param file content to share
     * @param name unique id
     * @param listener object to notify about progress
     */
    public void shareFile(File file, String name, IFileShareListener listener){
        ASharedFile serverFile;
        
        if(file.getName().matches(SharedTextFile.TEXT_FILES)){
            serverFile = new SharedTextFile(file, name, shareThread, encryptor, ConfigLoader.getInt("dead-time", 2000), listener);
        }else{
            serverFile = new SharedBinaryFile(file, name, shareThread, encryptor, ConfigLoader.getInt("dead-time", 2000), listener);
        }
        
        sharedFiles.put(name, serverFile);
        
        Thread sharing = new Thread(serverFile);
        sharing.setDaemon(true);
        sharing.start();
    }
    
    /**
     * Automatically called from AGroupThread when received request for file part 
     * @param messageSplit received text info from Datagram
     * @param source source sending IP
     */
    public void onFileSharePartRequest(String[] messageSplit, InetAddress source){
        if(isLocalAddress(source)) return;
        
        ASharedFile requested = sharedFiles.get(messageSplit[0]);
        if(requested != null) requested.partRequest(Integer.parseInt(messageSplit[1]));
    }
    
    /**
     * Automatically called from AGroupThread when received new text file part 
     * @param messageSplit received text info from Datagram
     * @param source source sending IP
     */
    public void onTextPart(String[] messageSplit, InetAddress source){
        if(isLocalAddress(source)) return;
        
        String fileName = messageSplit[0];
        AReceivedFile file = receivedFiles.get(fileName);
        
        if (file == null) {
            file = new ReceivedTextFile(fileName, Integer.parseInt(messageSplit[2]), shareThread, encryptor, source);
            receivedFiles.put(fileName, file);
        }
        
        //in case of splitting because of DELIMITER...
        String line =  Datagrams.reconstructMessage(messageSplit, 4);

        if (file.isPartValid(line, Integer.parseInt(messageSplit[3]))) {
            file.setPart(Integer.parseInt(messageSplit[1]), line);
        }
    }
    
    /**
     * Automatically called from AGroupThread when received new binary file part 
     * @param messageSplit received text info from Datagram
     * @param datagramData received binary data from Datagram
     * @param source source sending IP
     */
    public void onBinaryPart(String[] messageSplit, byte[] datagramData, InetAddress source){
        if(isLocalAddress(source)) return;
        
        AReceivedFile binaryFile = receivedFiles.get(messageSplit[0]);
        
        if (binaryFile == null) {
            binaryFile = new ReceivedBinaryFile(messageSplit[0], Integer.parseInt(messageSplit[2]), shareThread, encryptor, source);
            receivedFiles.put(messageSplit[0], binaryFile);
        }

        if (binaryFile.isPartValid(datagramData, Integer.parseInt(messageSplit[3]))) {
             binaryFile.setPart(Integer.parseInt(messageSplit[1]), datagramData);
        }
    }
    
    /**
     * Automatically called from AGroupThread when received FILE_SHARE_FINISH
     * @param messageSplit received text info from Datagram
     * @param source source sending IP
     */
    public void onFileShareFinish(String[] messageSplit, InetAddress source){
        if(isLocalAddress(source)) return;
        
        AReceivedFile finishedFile = receivedFiles.get(messageSplit[0]);
        if (finishedFile != null) {
           finishedFile.finished();
        } else {
           Logger.getLogger(ConfigLoader.class.getName()).log(Level.WARNING, "Shared file not found: {0}", messageSplit[0]);
        }//else it's too late to request new 
    }
    
    private boolean isLocalAddress(InetAddress address){
        try{
           return (NetworkInterface.getByInetAddress(address) != null);
        }catch(Exception e){}
        
        return false;
    }
    
}
