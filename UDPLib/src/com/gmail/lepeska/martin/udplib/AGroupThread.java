package com.gmail.lepeska.martin.udplib;

import com.gmail.lepeska.martin.udplib.util.Encryptor;
import com.gmail.lepeska.martin.udplib.client.GroupUser;
import com.gmail.lepeska.martin.udplib.datagrams.ADatagram;
import com.gmail.lepeska.martin.udplib.files.FileSharing;
import java.io.File;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import com.gmail.lepeska.martin.udplib.files.IFileShareListener;
import java.net.UnknownHostException;
import java.util.Objects;

/**
 * Common methods and components of GroupRunnables.
 * 
 * @author Martin Lepeška
 */
public abstract class AGroupThread extends Thread{
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
   protected final int port;
   
   /**This is UGLY, but necessary - multicast does not mean multi interface*/
   protected final LinkedList<MulticastSocket> sendSockets = new LinkedList<>();
   
   //AUTHENTICATION
   /**User's name in network*/
   protected final String userName;
   /**Group password*/
   protected final String groupPassword;
   
   //DATA
   /**Received messages*/
   protected final List<StoredMessage> messages= Collections.synchronizedList(new LinkedList<>());
   /**Class responsible for encrypting and decrypting messages*/
   protected final Encryptor encryptor;
   
   //USER
   /**User defined listeners*/
   protected List<IGroupListener> listeners = Collections.synchronizedList(new LinkedList<>());

   protected final FileSharing fileSharing;
   
    public AGroupThread(String userName, String groupPassword, int port) throws UnknownHostException{
        setDaemon(true);
        Objects.requireNonNull(userName);
        this.userName = userName;
        this.groupPassword = groupPassword;
        this.port = port;
        this.encryptor = (groupPassword!=null)?new Encryptor(groupPassword):new Encryptor();
        this.fileSharing = new FileSharing(this, encryptor);
    }
   
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
    * Clears messages from thread cache.
    * @return Messages received from last time of pickMessages() call
    */
   public List<StoredMessage> pickMessages(){ 
       ArrayList<StoredMessage> pickedMessages = new ArrayList<>();
       for(StoredMessage msg : messages){
           pickedMessages.add(msg);
       }
       messages.clear();
       
       return pickedMessages;
   }
   
   /**
    * Stops this thread
    */
   protected void finishThread(){
      if(socket != null){
          socket.close();
          socket = null;
      }
      
      listeners.stream().forEach((listener) -> {
        listener.kicked();
      });
   }
   
   /**
    * Adds given listener to current listeners
    * @param listener User's class responsible for dealing with events
    */
   public void addListener(IGroupListener listener){
       listeners.add(listener);
   }
   
   /**
    * Removes given listener from current listeners
    * @param listener User's class responsible for dealing with events
    */
   public void removeListener(IGroupListener listener){
       listeners.remove(listener);
   }
   
  /**
   * Ends communication(GroupServerThread destroys network)
   */
   public abstract void leave();

   /**
    * Deals with received datagram.
    * @param source DatagramPacket containing UDPLib datagram
    * @param datagram  reconstructed datagram
    */
   protected final void dealWithDatagram(DatagramPacket source, ADatagram datagram){
       String[] messageSplit = datagram.getStringMessage();
       byte[] datagramData = datagram.getBinaryMessage();
       InetAddress sourceAddress = source.getAddress();
       
       switch(datagram.getType()){
            case FILE_SHARE_PART_REQUEST:   fileSharing.onFileSharePartRequest(messageSplit, sourceAddress);
                                            break;
            case TEXT_FILE_SHARE_PART:      fileSharing.onTextPart(messageSplit, sourceAddress);
                                            break;
            case BINARY_FILE_SHARE_PART:    fileSharing.onBinaryPart(messageSplit, datagramData, sourceAddress);
                                            break;
            case FILE_SHARE_FINISH:         fileSharing.onFileShareFinish(messageSplit, sourceAddress);
                                            break;
            default:    childDealWithDatagram(source, datagram);
       }
   }

   protected abstract void childDealWithDatagram(DatagramPacket source, ADatagram datagram);
   
   /**
    * Sends datagram with given data to specified GroupUser
    * @param target who should receive this datagram
    * @param datagram what should user receive
    */
   public synchronized void sendDatagram(InetAddress target, ADatagram datagram){
       try{
           DatagramPacket packet = datagram.createPacket(target, port);
           socket.send(packet);
       }catch(Exception e){
           throw new UDPLibException("Unable to send datagram: ", e);
       }
   }

   /**
    * Sends datagram with given data to specified GroupUser
    * @param target User, who should receive this datagram
    * @param datagram what should user receive
    */
   public synchronized void sendDatagram(GroupUser target, ADatagram datagram){
       sendDatagram(target.ip, datagram);
   }
   
   /**
    * Sends datagram with given data to all GroupUsers
    * @param datagram what should everyone receive
    */
   public synchronized void sendMulticastDatagram(ADatagram datagram){
       try{
          DatagramPacket packet = datagram.createPacket(groupAddress, port);
          for(MulticastSocket sendSocket: sendSockets){
            sendSocket.send(packet);
          }
       }catch(Exception e){
           throw new UDPLibException("Unable to send datagram: ", e);
       }
   }
   
   /**
    * Adds new message to collection and notifies listener
    * @param message received message
    */
   protected void addMessage(StoredMessage message){
       messages.add(message);
       
       listeners.stream().forEach((listener) -> {
            listener.mesageReceived();
        });
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
   
   /**
    * @return latest info about group users
    */
   public abstract List<GroupUser> getCurrentGroupUsers();
   
   /**
    * 
    * @param ip ip of the interface
     * @return NetworkInterface with given ip or null, if no interface found
     * @throws java.net.SocketException 
    */
   public NetworkInterface getInterfaceByIP(String ip) throws SocketException{
       Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface netint : Collections.list(nets)){
            Enumeration<InetAddress> addresses = netint.getInetAddresses();
            for(InetAddress addr : Collections.list(addresses)){
                if(addr.getHostAddress().equals(ip)){
                    return netint;
                }
            }
        }
        return null;
   }   
   
   
    /**
     * Automatically called from SharedFile, when ti creates new temporary file
     *
     * @param file temporary file containing shared content from network
     */
    public void receiveFile(File file) {
        listeners.stream().forEach((listener) -> {
            listener.fileReceived(file);
        });
    }
   
    /**
     * @param file content to share
     * @param name unique id
     * @param listener object to notify about progress
     */
    public void shareFile(File file, String name, IFileShareListener listener){
        fileSharing.shareFile(file, name, listener);
    }
    
}