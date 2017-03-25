package com.gmail.lepeska.martin.udplib.files;

import com.gmail.lepeska.martin.udplib.AGroupThread;
import com.gmail.lepeska.martin.udplib.UDPLibException;
import com.gmail.lepeska.martin.udplib.datagrams.ADatagram;
import com.gmail.lepeska.martin.udplib.datagrams.files.FileShareFinish;
import com.gmail.lepeska.martin.udplib.util.Encryptor;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class representing file, which will be divided into segments and shared to 
 * GroupNetwork.
 * 
 * @author Martin Lepeška
 */
public abstract class ASharedFile implements Runnable{
    protected final File fileToShare;
    final String name;
    protected final AGroupThread groupThread;
    private final int deadTime;
    protected final Encryptor encryptor;
    private final IFileShareListener listener;
    
    /**Flag, that thread should not finish this cycle*/
    protected volatile boolean wasRequest = true;
    
    /**
     * @param fileToShare Data to share
     * @param name Unique String ID, under which will be file accessible at AGroupNetwork class
     * @param groupThread Thread responsible for sending datagrams to group
     * @param encryptor Class responsible for encrypting file content on network
     * @param deadTime Time, which will server wait after finishing file sharing. At this time client may request resending some of the parts
     * @param listener can be null, object which will be notified about success/fail
     */
    public ASharedFile(File fileToShare, String name, AGroupThread groupThread, Encryptor encryptor, int deadTime, IFileShareListener listener){
        this.fileToShare = fileToShare;
        this.deadTime = deadTime;
        this.groupThread = groupThread;
        this.name = name;
        this.encryptor = encryptor;
        this.listener = listener;
    }
    
    @Override
    public void run() {
        try{
            readFile();

            sendPartDatagrams();
            
            ADatagram datagram = new FileShareFinish(encryptor, name);
            
            while(wasRequest){
                wasRequest = false;
                groupThread.sendMulticastDatagram(datagram);
                
                Thread.sleep(deadTime);
            }
            
            if(listener != null) listener.onFinished(fileToShare);
        }catch(InterruptedException e){
             Logger.getLogger(ASharedFile.class.getName()).log(Level.WARNING, "Shutting down ServerSharedFileThread...");
             if(listener != null) listener.onFail(fileToShare, e);
        }catch(IOException ex){
            if(listener != null) listener.onFail(fileToShare, ex);
            throw new UDPLibException("File sharing problem: ", ex);
        }
    }
    
    /**
     * Sends parts loaded by readFile() to clients
     * @throws InterruptedException 
     */
    protected abstract void sendPartDatagrams() throws InterruptedException;
    
    /**
     * Loads file given in constructor
     * @throws IOException
     * @throws UDPLibException 
     */
    protected abstract void readFile() throws IOException, UDPLibException;
    
    /**
     * Called from server thread, when it received CLIENT_FILE_SHARE_PART_REQUEST
     * @param index index of requested part
     */
    public abstract void partRequest(int index);
    
}