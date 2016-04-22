package com.gmail.lepeska.martin.udplib;

import com.gmail.lepeska.martin.udplib.client.GroupUser;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Common methods and components of GroupRunnables.
 * 
 * @author Martin Lepeška
 */
public abstract class IGroupRunnable implements Runnable{
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
   
   //DATA
   /**Received messages*/
   protected List<StoredMessage> messages= Collections.synchronizedList(new LinkedList<>());
   protected Encryptor encryptor;
   
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
    * Deals with received datagram.
    * @param source DatagramPacket containing UDPLib datagram
    * @param type type of UDPLib datagram
    * @param data  decrypted data of datagram
    */
   protected abstract void dealWithPacket(DatagramPacket source, DatagramTypes type, byte[] data);
   
  /**
   * Ends communication(GroupServerThread destroys network)
   */
   public abstract void leave();
   
   /**
    * Sends datagram with given data to specified GroupUser
    * @param target who should receive this datagram
    * @param data what should user receive
    */
   public synchronized void sendDatagram(InetAddress target, byte[] data){
       try{
           DatagramPacket packet = new DatagramPacket(data, data.length, target, port); 
           socket.send(packet);
       }catch(Exception e){
           throw new UDPLibException("Unable to send datagram: ", e);
       }
   }
   
   /**
    * Sends datagram with given data to specified GroupUser
    * @param target User, who should receive this datagram
    * @param data what should user receive
    */
   public synchronized void sendDatagram(GroupUser target, byte[] data){
       sendDatagram(target.ip, data);
   }
   
   /**
    * Sends datagram with given data to all GroupUsers
    * @param data what should everyone receive
    */
   public synchronized void sendMulticastDatagram(byte[] data){
       try{
           DatagramPacket packet = new DatagramPacket(data, data.length, groupAddress, port); 
           socket.send(packet);
       }catch(Exception e){
           throw new UDPLibException("Unable to send datagram: ", e);
       }
   }
   
   /**
    * Encodes(id password is used) and sends given message to specified GroupUser
    * @param target User, who should receive this message
    * @param message String, which should user receive
    */
   public abstract void sendMessage(GroupUser target, String message);
   
   /**
    * Encodes(id password is used) and sends given message to all GroupUsers
    * @param message String, which should everyone receive
    */
   public abstract void sendMulticastMessage(String message);
}