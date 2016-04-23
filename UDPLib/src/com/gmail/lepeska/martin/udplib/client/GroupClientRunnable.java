package com.gmail.lepeska.martin.udplib.client;

import com.gmail.lepeska.martin.udplib.ConfigLoader;
import com.gmail.lepeska.martin.udplib.DatagramTypes;
import com.gmail.lepeska.martin.udplib.Datagrams;
import com.gmail.lepeska.martin.udplib.Encryptor;
import com.gmail.lepeska.martin.udplib.IGroupRunnable;
import com.gmail.lepeska.martin.udplib.StoredMessage;
import com.gmail.lepeska.martin.udplib.UDPLibException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mytrin
 */
public class GroupClientRunnable  extends IGroupRunnable{
    
    /**Users in group*/
    private final ArrayList<GroupUser> groupUsers= new ArrayList<>();

    /**Expected server address*/
    private final InetAddress serverAddress;
    
    public static void main(String[] args) throws UnknownHostException{
        if(ConfigLoader.loadConfig()){
            new Thread(new GroupClientRunnable("Max", null, "25.146.244.201")).start();
        }
    }
    
    /**
     * Creates new GroupClientRunnable bound on interface of given hostAddress with given password and server address.
     * 
     * @param userName User's name in network
     * @param groupPassword Password required to access this group or null, if none
     * @param clientAddress Address in network interface, which should server socket use
     * @param serverAddress Address of group owner
     * @param port Port of server socket
     * @throws UnknownHostException 
     */
    public GroupClientRunnable(String userName, String groupPassword, String clientAddress, String serverAddress, int port) throws UnknownHostException{
        Objects.requireNonNull(userName);
        this.groupUsers.add(new GroupUser(userName, InetAddress.getByName(clientAddress)));
        this.userName = userName;
        this.groupPassword = groupPassword;
        this.hostAddress = InetAddress.getByName(clientAddress);
        this.serverAddress = InetAddress.getByName(serverAddress);
        this.port = port;
        this.encryptor = (groupPassword!=null)?new Encryptor(groupPassword):new Encryptor();
    }
    
    /**
     * Creates new GroupClientRunnable with given password and default values loaded from configuration file.
     * 
     * @param userName User's name in network
     * @param groupPassword Password required to access this group or null, if none
     * @param serverAddress Address of group owner
     * @throws UnknownHostException 
     */
    public GroupClientRunnable(String userName, String groupPassword, String serverAddress) throws UnknownHostException{
        this(userName, groupPassword, ConfigLoader.getString("default-server-ip"), serverAddress, ConfigLoader.getInt("default-port"));
    }
    
    /**
     * Creates important components, which could not been created in constructor, before starting loop.
     */
    private void init(){
        try{
            groupUsers.add(new GroupUser(userName, hostAddress));
            
            socket = new MulticastSocket(new InetSocketAddress(hostAddress, port));
            
            byte[] data = Datagrams.createClientAccessRequest(encryptor, userName, groupPassword);
            sendDatagram(serverAddress, data);

            byte[] responseData = new byte[Datagrams.MAXIMUM_DATA_LENGTH];
            DatagramPacket response = new DatagramPacket(responseData, port);
            
            socket.receive(response);
            
            byte[] decryptedReponse = encryptor.decrypt(responseData);
            String responseStr = new String(Datagrams.unpack(decryptedReponse)).trim();
            
            
            if(Datagrams.getDatagramType(responseData) == DatagramTypes.SERVER_ACCEPT_CLIENT_RESPONSE){
                String[] responseSplit = responseStr.split(Datagrams.DELIMITER);
                
                if(responseSplit.length < 2){
                    throw new UDPLibException("Received response with uncomplete data!"+responseStr);
                }
                
                if(responseSplit[1].equals("1")){
                    groupAddress = InetAddress.getByName(responseSplit[0]);
                    socket.joinGroup(groupAddress);
                }else{
                    throw new UDPLibException("Wrong password!");
                }
            }else{
                throw new UDPLibException("Received garbage response: "+responseStr);
            }
        }catch(SocketTimeoutException ex){
            finishThread();
            Logger.getLogger(ConfigLoader.class.getName()).log(Level.SEVERE, "Server did not responded! ", ex);
            
            throw new UDPLibException("Server did not responded! ", ex);
        }catch(IOException | UDPLibException e){
           finishThread();
           Logger.getLogger(ConfigLoader.class.getName()).log(Level.SEVERE, "Thread shut down! ", e);
                      
           throw new UDPLibException("Thread shut down! ", e);
        }
    }
    
    @Override
    public void run() {
        init();
        
        while(!Thread.currentThread().isInterrupted() && socket != null){
            try{
                byte[] buf = new byte[Datagrams.MAXIMUM_DATA_LENGTH];
                byte[] decryptedBuf;

                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                decryptedBuf = encryptor.decrypt(buf);
                DatagramTypes    type = Datagrams.getDatagramType(decryptedBuf);
                    
                System.out.println("-> "+new String(decryptedBuf).trim()+" type: "+type.name());
                    
                if(type != DatagramTypes.TRASH){
                    dealWithPacket(packet, type, Datagrams.unpack(decryptedBuf));
                }else{
                    //trash    
                    Logger.getLogger(ConfigLoader.class.getName()).log(Level.WARNING, "Trash received: {0} -> {1} ", new String[]{new String(buf), new String(decryptedBuf)});
                }

            }catch(Exception e){
                 Logger.getLogger(ConfigLoader.class.getName()).log(Level.SEVERE, "Error when parsing datagram! ", e);
            }
        }
        
        finishThread();
    }

    @Override
    protected void dealWithPacket(DatagramPacket source, DatagramTypes type, byte[] data) {
        String[] messageSplit = new String(data).split(Datagrams.DELIMITER);
        GroupUser user;

        try{
            switch(type){
                case SERVER_CLIENTS_INFO: user = findGroupUserbyName(messageSplit[0]);
                                      if(user != null){
                                          user.setPingToHost(Long.parseLong(messageSplit[2]));
                                      }else{
                                          user = new GroupUser(messageSplit[0], InetAddress.getByName(messageSplit[1]));
                                          user.setPingToHost(Long.parseLong(messageSplit[2]));
                                          groupUsers.add(user);
                                      }
                                      break;
                case SERVER_CLIENT_DEAD:  user = findGroupUserbyName(messageSplit[0]);
                                      if(user != null){
                                          groupUsers.remove(user);
                                          
                                          if(user.name.equals(userName) && user.ip.equals(hostAddress)){
                                              finishThread();
                                              throw new UDPLibException("Kicked out from group!");
                                          }
                                      }
                                      break;
                case SERVER_IS_ALIVE_REQUEST: sendDatagram(serverAddress, Datagrams.createIsAliveResponseDatagram(encryptor));
                                          break;
                case CLIENT_UNICAST_MESSAGE:    user = findGroupUserbyInetAddr(source.getAddress());
                                            messages.add(new StoredMessage(new String(data), user, false));
                                            break;
                case CLIENT_MULTICAST_MESSAGE:  user = findGroupUserbyInetAddr(source.getAddress());
                                            messages.add(new StoredMessage(new String(data), user, true));
                                            break;
                case SERVER_UNICAST_MESSAGE: messages.add(new StoredMessage(new String(data), findGroupUserbyName(userName), false));
                                         break;
                case SERVER_MULTICAST_MESSAGE: messages.add(new StoredMessage(new String(data), findGroupUserbyName(userName), true));
                                         break;
            }
        }catch(NumberFormatException | UnknownHostException | UDPLibException e){
            Logger.getLogger(ConfigLoader.class.getName()).log(Level.SEVERE, "Error when parsing datagram! ", e);
        }
    }
    
    @Override
    public void leave() {
        Thread.currentThread().interrupt();
    }
    
    @Override
    public void sendMessage(GroupUser target, String message) {
        byte[] data = Datagrams.createMessageDatagram(encryptor, message, false, this);
        sendDatagram(target, data);
    }

    @Override
    public void sendMulticastMessage(String message) {
        byte[] data = Datagrams.createMessageDatagram(encryptor, message, false, this);
        sendMulticastDatagram(data);
    }

    @Override
    public GroupUser[] getGroupUsers() {
        return (GroupUser[])groupUsers.toArray();
    }

    private GroupUser findGroupUserbyInetAddr(InetAddress ip){
        for(GroupUser user: groupUsers){
            if(user.ip.equals(ip)){
                return user;
            }
        }
        return null;
    }
    
    private GroupUser findGroupUserbyName(String userName){
        for(GroupUser user: groupUsers){
            if(user.name.equals(userName)){
                return user;
            }
        }
        return null;
    }
}