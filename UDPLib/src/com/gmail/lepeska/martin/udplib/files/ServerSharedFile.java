package com.gmail.lepeska.martin.udplib.files;

import com.gmail.lepeska.martin.udplib.Datagrams;
import com.gmail.lepeska.martin.udplib.UDPLibException;
import com.gmail.lepeska.martin.udplib.server.GroupServerThread;
import com.gmail.lepeska.martin.udplib.util.Encryptor;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class representing file, which will be divided into segments and shared to 
 * GroupNetwork.
 * 
 * @author Martin Lepeška
 */
public class ServerSharedFile implements Runnable{
    /**List of supported text files, if your format is not listed here, you can change it*/
    public static String TEXT_FILES = ".*\\.(txt|pdf|xml|json|js|html|htm|php|java|css|doc|docx|odt|xls|csv|py|lua|svg|conf|log|ini|yaml)";
    
    private final ArrayList<String> parts = new ArrayList<>();
    
    private final File fileToShare;
    private final String name;
    private final GroupServerThread groupServer;
    private final int deadTime;
    private final Encryptor encryptor;
    private final IServerShareListener listener;
    
    /**Flag, that thread should not finish this cycle*/
    private volatile boolean wasRequest = true;
    
    /**Time which thread waits between sending datagrams, so it does not occupy socket too long*/
    private static final long WAITING_TIME = 10;
    
    /**
     * @param fileToShare Data to share
     * @param name Unique String ID, under which will be file accessible at AGroupNetwork class
     * @param groupServer Thread responsible for sending datagrams to group
     * @param encryptor Class responsible for encrypting file content on network
     * @param deadTime Time, which will server wait after finishing file sharing. At this time client may request resending some of the parts
     * @param listener can be null, object which will be notified about success/fail
     */
    public ServerSharedFile(File fileToShare, String name, GroupServerThread groupServer, Encryptor encryptor, int deadTime, IServerShareListener listener){
        this.fileToShare = fileToShare;
        this.deadTime = deadTime;
        this.groupServer = groupServer;
        this.name = name;
        this.encryptor = encryptor;
        this.listener = listener;
    }

    /**
     * Makes sure, that lines are not too large to be sent across network
     * by splitting the large ones
     */
    private void validate(){
        for(int i=0; i < parts.size(); i++){
            String line = parts.get(i);
            while(line.length() > Datagrams.MAXIMUM_DATA_LENGTH/2){
                String cutted = line.substring(line.length()/2);
                line = line.substring(0, line.length()/2);
                
                parts.set(i, line);
                parts.add(i+1, cutted);
            }
        }
    }
    
    @Override
    public void run() {
        try{
            readFile();

            byte[] datagram;
            
            for(int i = 0; i < parts.size(); i++){
                datagram = Datagrams.createServerFileSharePart(encryptor, name, parts.get(i), i, parts.size());
                groupServer.sendMulticastDatagram(datagram);
                Thread.sleep(10);
            }
            
            datagram = Datagrams.createServerFileShareFinish(encryptor, name);
            
            while(wasRequest){
                wasRequest = false;
                groupServer.sendMulticastDatagram(datagram);
                
                Thread.sleep(deadTime);
            }
            
            if(listener != null) listener.onFinished(fileToShare);
        }catch(InterruptedException e){
             Logger.getLogger(ServerSharedFile.class.getName()).log(Level.WARNING, "Shutting down ServerSharedFileThread...");
             if(listener != null) listener.onFail(fileToShare, e);
        }catch(IOException ex){
            if(listener != null) listener.onFail(fileToShare, ex);
            throw new UDPLibException("File sharing problem: ", ex);
        }
    }
    
    private void readFile() throws IOException, UDPLibException{
        Path path = fileToShare.toPath();
        
        if(fileToShare.getName().matches(TEXT_FILES)){
            Files.lines(path).forEach((String t) -> {parts.add(t+"\n");});
        
            validate();
        }else{
            throw new UDPLibException("Cannot send binary file");
        }
    }
    
    /**
     * Called from server thread, when it received CLIENT_FILE_SHARE_PART_REQUEST
     * @param index index of requested part
     */
    public void partRequest(int index){
        wasRequest = true;
        byte[] datagram = Datagrams.createServerFileSharePart(encryptor, name, parts.get(index), index, parts.size());
        groupServer.sendMulticastDatagram(datagram);
    }
    
}