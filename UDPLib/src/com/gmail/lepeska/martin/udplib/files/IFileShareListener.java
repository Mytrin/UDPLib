package com.gmail.lepeska.martin.udplib.files;

import java.io.File;

/**
 *
 * Simple listener for server side of file sharing 
 * 
 * @author Martin Lepeška
 */
public interface IFileShareListener {
    
    /**
     * Called, when file has been successfully sent to network
     * @param file shared file
     */
    public void onFinished(File file);
    
    /**
     * Called, when file cannot be shared for some reason
     * @param file shared file
     * @param e reason, while sharing failed
     */
    public void onFail(File file, Exception e);
}
