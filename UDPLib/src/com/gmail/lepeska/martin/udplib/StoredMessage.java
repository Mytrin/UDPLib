package com.gmail.lepeska.martin.udplib;

import com.gmail.lepeska.martin.udplib.client.GroupUser;

/**
 * Simple data class for storing info about received message.
 * 
 * @author Martin Lepeška
 */
public class StoredMessage {
    /**Content*/
    public final String message;
    /**Sender name*/
    public final GroupUser sender;
    /**Private message?*/
    public final boolean isMulticast;
    
    /**
     * 
     * @param message Content
     * @param sender source of this message 
     * @param isMulticast true, if message was sent to more users
     */
    public StoredMessage(String message,GroupUser sender,boolean isMulticast){
        this.message=message;
        this.sender=sender;
        this.isMulticast = isMulticast;
    }
    
}