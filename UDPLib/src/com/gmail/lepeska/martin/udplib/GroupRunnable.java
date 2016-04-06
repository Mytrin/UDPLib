package com.gmail.lepeska.martin.udplib;

import com.gmail.lepeska.martin.udplib.client.GroupUser;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Common methods and components of GroupRunnables.
 * 
 * @author Martin Lepeška
 */
public abstract class GroupRunnable implements Runnable{
   //NETWORK
   /**
   * Socket which sends and receives messages(full duplex). 
   */
   protected MulticastSocket socket; 
    /**Group IP address*/
   protected InetAddress groupAddress;
   /**Host's IP address*/
   protected InetAddress hostAddress;
   /**Used port*/
   protected int port;
   
   //AUTHENTICATION
   /**User's name in network*/
   protected String userName;
   /**Group password*/
   protected String groupPassword="none";
   /**Indicates, if topology changed since last call of usersChanged() */
   protected AtomicBoolean usersChanged = new AtomicBoolean(false);
   
   //DATA
   /**Received messages*/
   protected List<StoredMessage> messages= Collections.synchronizedList(new LinkedList<>());
   
   //THREAD
    /** Indicator, that thread is running*/
   protected boolean running=true;  
   
   /**
    * @return Group IP
    */
   public InetAddress getGroupAddress(){
       return groupAddress;
   }
   
   /**
    * @return host IP
    */
   public InetAddress getHostAddress(){
       return hostAddress;
   }
   
   /**
    * @return Your name in group
    */
   public String getUserName() {
	return userName;
   }
   
   /**
    * @return true, if topology changed since last call of usersChanged()
    */
    public boolean UsersChanged() {
        boolean toReturn = usersChanged.get();
        usersChanged.set(false);
        return toReturn;
    }

   /**
    * @return List of network members
    */
   public abstract GroupUser[] getGroupUsers();
   
   /**
    * Clears messages from thread cache.
    * @return Messages received from last time of pickMessages() call
    */
   public StoredMessage[] pickMessages(){ 
       StoredMessage[] store;
       if(!messages.isEmpty()){
           store=messages.toArray(new StoredMessage[messages.size()]);
           messages.clear();
       }else{
           store=new StoredMessage[0];
       }
       return store;
   }
   
   /**
    * Stops this thread
    */
   protected void finishThread(){
      if(socket != null){
          socket.close();
      }
      running = false;
   }
   
  /**
   * Ends communication(GroupServerThread destroys network)
   */
   public abstract void leave();
}