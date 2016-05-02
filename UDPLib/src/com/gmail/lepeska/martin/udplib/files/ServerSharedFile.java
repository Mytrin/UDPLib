package com.gmail.lepeska.martin.udplib.files;

import com.gmail.lepeska.martin.udplib.Datagrams;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

/**
 * Class representing file, which will be divided into segments and shared to 
 * GroupNetwork.
 * 
 * @author Martin Lepeška
 */
public class ServerSharedFile implements Runnable{

    private final ArrayList<String> parts = new ArrayList<>();
    
    /**
     * @param fileToShare data to share
     * @param name unique String ID, under which will be file accessible at
     * AGroupNetwork class
     * @throws java.io.IOException
     */
    public void SharedFile(File fileToShare, String name) throws IOException{
        Files.lines(fileToShare.toPath()).forEach((String t) -> {parts.add(t+"\n");});
        validate();
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
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}