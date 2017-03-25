package com.gmail.lepeska.martin.udplib;

import com.gmail.lepeska.martin.udplib.client.GroupUser;
import com.gmail.lepeska.martin.udplib.files.IFileShareListener;
import java.io.File;
import java.util.List;
import java.util.Objects;

/**
 * This class serves as a cover over UDP multicast communication.
 * It enables to easily send messages and files to other clients.
 * There are two types of GroupNetwork - HostGroupNetwork and ClientGroupNetwork 
 * Clients usually send all important messages to host, who sends them to all clients.
 * Host keeps and updates info about network, its users and their "ping" to him.
 *  
 * Group IP(224.0.0.0 to 239.255.255.255)
 * 
 * @author Martin Lepeška
 */
public abstract class AGroupNetwork {
    /**Implementing thread*/
    protected AGroupThread groupThread;
    
    /**
     * Starts connecting or creation of GroupNetwork depending on implementing class
     */
    public void start(){
        groupThread.start();
    }
    
    /**
     * Leaves current GroupNetwork
     */
    public void leave(){
        groupThread.leave();
    }
    
    /**
     * @return received messages since last call of getMessages()
     */
    public List<StoredMessage> getMessages(){
        return groupThread.pickMessages();
    }
    
    /**
     * @return latest info about group users
     */
    public List<GroupUser> getCurrentUsers(){
        return groupThread.getCurrentGroupUsers();
    }
    
    /**
     * Sends message to other client in GroupNetwork
     * @param target recipient
     * @param message data
     */
    public void sendMessage(GroupUser target, String message){
        groupThread.sendMessage(target, message);
    }
    
    /**
     * Sends message to other clients in GroupNetwork
     * @param message data
     */
    public void sendGroupMessage(String message){
        groupThread.sendMulticastMessage(message);
    }
    
   /**
    * Adds given listener to current listeners
    * @param listener User's class responsible for dealing with events
    */
   public void addListener(IGroupListener listener){
       groupThread.addListener(listener);
   }
   
   /**
    * Removes given listener from current listeners
    * @param listener User's class responsible for dealing with events
    */
   public void removeListener(IGroupListener listener){
       groupThread.removeListener(listener);
   }

   /**
    * @return User's name in group
    */
   public String getUserName() {
       return groupThread.getUserName();
   }
   
    /**
    * Sends content of file into group, to share it with other client.
     * 
     * @param file content to share
     * @param name unique id
     * @param listener object to notify about progress
     */
    public void shareFile(File file, String name, IFileShareListener listener){
       Objects.requireNonNull(file);
       groupThread.shareFile(file, name, listener);
    }
}