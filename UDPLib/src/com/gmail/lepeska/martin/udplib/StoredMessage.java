package com.gmail.lepeska.martin.udplib;

/**
 * Simple data class for storing info about received message.
 * 
 * @author Martin Lepeška
 */
public class StoredMessage {
    /**Content*/
    public String message="";
    /**Sender name*/
    public String senderName="";
    /**Sender IP*/
    public String senderIP="";
    
    /**
     * 
     * @param message Content
     * @param senderName Sender name
     * @param senderIP  Sender IP
     */
    StoredMessage(String message,String senderName,String senderIP){
        this.message=message;
        this.senderName=senderName;
        this.senderIP=senderIP;
    }
    
}