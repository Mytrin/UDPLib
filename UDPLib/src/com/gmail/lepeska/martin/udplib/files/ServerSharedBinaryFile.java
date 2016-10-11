package com.gmail.lepeska.martin.udplib.files;

import com.gmail.lepeska.martin.udplib.Datagrams;
import com.gmail.lepeska.martin.udplib.UDPLibException;
import com.gmail.lepeska.martin.udplib.server.GroupServerThread;
import com.gmail.lepeska.martin.udplib.util.ConfigLoader;
import com.gmail.lepeska.martin.udplib.util.Encryptor;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Class representing text file, which will be divided into segments and shared
 * to GroupNetwork.
 *
 * @author Martin Lepeška
 */
public class ServerSharedBinaryFile extends AServerSharedFile {

    private byte[] data;
    private int partsCount;
    
    /**
     * @param fileToShare Data to share
     * @param name Unique String ID, under which will be file accessible at
     * AGroupNetwork class
     * @param groupServer Thread responsible for sending datagrams to group
     * @param encryptor Class responsible for encrypting file content on network
     * @param deadTime Time, which will server wait after finishing file
     * sharing. At this time client may request resending some of the parts
     * @param listener can be null, object which will be notified about
     * success/fail
     */
    public ServerSharedBinaryFile(File fileToShare, String name, GroupServerThread groupServer, Encryptor encryptor, int deadTime, IServerShareListener listener) {
        super(fileToShare, name, groupServer, encryptor, deadTime, listener);
    }

    @Override
    protected void readFile() throws IOException, UDPLibException {
        Path path = fileToShare.toPath();

        data = Files.readAllBytes(path);
        
        partsCount = (int)Math.ceil(data.length/Datagrams.MAXIMUM_DATA_LENGTH);
    }

    private byte[] createPartDatagram(int index){
        byte[] datagram;
        byte[] datagramData;
        
        int datagramStart = index*Datagrams.MAXIMUM_DATA_LENGTH;
        int datagramLength;
        if(datagramStart+Datagrams.MAXIMUM_DATA_LENGTH<data.length){
            datagramLength = Datagrams.MAXIMUM_DATA_LENGTH;
        }else{
            datagramLength = data.length - datagramStart;
        }
        
        datagramData = new byte[datagramLength];
        System.arraycopy(data, datagramStart, datagramData, 0, datagramLength);
            
        datagram = Datagrams.createServerBinaryFileSharePart(encryptor, name, datagramData, index, partsCount);
        
        return datagram;
    } 
            
    @Override
    protected void sendPartDatagrams() throws InterruptedException{
        int waitingTime = ConfigLoader.getInt("file-time");
        
        for(int i = 0; i < partsCount; i++){
            groupServer.sendMulticastDatagram(createPartDatagram(i));            
            Thread.sleep(waitingTime);
        }
    }
        
    @Override
    public void partRequest(int index) {
        wasRequest = true;
        groupServer.sendMulticastDatagram(createPartDatagram(index));
    }
}