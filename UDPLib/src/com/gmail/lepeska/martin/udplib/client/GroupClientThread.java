package com.gmail.lepeska.martin.udplib.client;

import com.gmail.lepeska.martin.udplib.ConfigLoader;
import com.gmail.lepeska.martin.udplib.DatagramTypes;
import com.gmail.lepeska.martin.udplib.Datagrams;
import com.gmail.lepeska.martin.udplib.Encryptor;
import com.gmail.lepeska.martin.udplib.AGroupThread;
import com.gmail.lepeska.martin.udplib.StoredMessage;
import com.gmail.lepeska.martin.udplib.UDPLibException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Martin Lepeška
 */
public class GroupClientThread  extends AGroupThread{
    
    /**Users in group*/
    private final ArrayList<GroupUser> groupUsers= new ArrayList<>();

    /**Expected server address*/
    private final InetAddress serverAddress;

    /**
     * Creates new GroupClientRunnable bound on interface of given hostAddress with given password and server address.
     * 
     * @param userName User's name in network
     * @param groupPassword Password required to access this group or null, if none
     * @param serverAddress Address of group owner
     * @param port Port of server socket
     * @throws UnknownHostException 
     */
    public GroupClientThread(String userName, String groupPassword, String serverAddress, int port) throws UnknownHostException{
        Objects.requireNonNull(userName);
        this.userName = userName;
        this.groupPassword = groupPassword;
        this.serverAddress = InetAddress.getByName(serverAddress);
        this.port = port;
        this.encryptor = (groupPassword!=null)?new Encryptor(groupPassword):new Encryptor();
        setDaemon(true);
    }
    
    /**
     * Creates important components, which cannot be created in constructor, before starting loop.
     */
    private void init(){
        try{
            socket = new MulticastSocket(port);
            sendSockets.add(socket);
            
            byte[] data = Datagrams.createClientAccessRequest(encryptor, userName, groupPassword);
            sendDatagram(serverAddress, data);

            byte[] responseData = new byte[Datagrams.MAXIMUM_DATA_LENGTH];
            DatagramPacket response = new DatagramPacket(responseData, responseData.length);
            
            socket.setSoTimeout(ConfigLoader.getInt("dead-time"));
            socket.receive(response);
            
            String decryptedReponse = Datagrams.unpack(responseData, encryptor, response);
            String responseStr = Datagrams.unpack2(decryptedReponse);
            
            if(Datagrams.getDatagramType(decryptedReponse) == DatagramTypes.SERVER_ACCEPT_CLIENT_RESPONSE){
                String[] responseSplit = responseStr.split(Datagrams.DELIMITER);
                
                if(responseSplit.length < 3){
                    throw new UDPLibException("Received response with uncomplete data!"+responseStr);
                }
                
                if(responseSplit[1].equals("1")){
                    groupAddress = InetAddress.getByName(responseSplit[0]);
                    //no other way to know from which interface was server contacted
                    hostAddress = InetAddress.getByName(responseSplit[2]); 
                    
                    NetworkInterface nic = NetworkInterface.getByInetAddress(hostAddress);
                    socket.joinGroup(new InetSocketAddress(responseSplit[0], port), nic);
                    
                    socket.setSoTimeout(ConfigLoader.getInt("user-info-period")*2);
                    
                    this.groupUsers.add(new GroupUser(userName, hostAddress));
                    
                    listeners.stream().forEach((listener) -> {
                      listener.joined();
                    });
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

                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                String data = Datagrams.unpack(buf, encryptor, packet);
                DatagramTypes type = Datagrams.getDatagramType(data);
                
                if(type != DatagramTypes.TRASH){
                    dealWithPacket(packet, type, Datagrams.unpack2(data));
                }else{
                    //trash    
                    Logger.getLogger(ConfigLoader.class.getName()).log(Level.WARNING, "Trash received: {0} -> {1} ", new String[]{Datagrams.bytesToString(buf), data});
                }

            }catch(Exception e){
                 Logger.getLogger(ConfigLoader.class.getName()).log(Level.SEVERE, "Error when parsing datagram! ", e);
            }
        }
        
        finishThread();
    }

    @Override
    protected void dealWithPacket(DatagramPacket source, DatagramTypes type, String data) {       
        String[] messageSplit = data.split(Datagrams.DELIMITER);
        GroupUser user;

        try{
            switch(type){
                case SERVER_CLIENTS_INFO: user = findGroupUserbyInetAddr(InetAddress.getByName((messageSplit[1])));
                                      if(user != null){
                                          user.setPingToHost(Long.parseLong(messageSplit[2]));
                                      }else{
                                          GroupUser newUser = new GroupUser(messageSplit[0], InetAddress.getByName(messageSplit[1]));
                                          newUser.setPingToHost(Long.parseLong(messageSplit[2]));
                                          groupUsers.add(newUser);
                                          listeners.stream().forEach((listener) -> {
                                                listener.userJoined(newUser);
                                          });
                                      }
                                      break;
                case SERVER_CLIENT_DEAD:  GroupUser kickedUser = findGroupUserbyInetAddr(InetAddress.getByName(messageSplit[1]));
                                      if(kickedUser != null){
                                          groupUsers.remove(kickedUser);
                                          
                                          if(kickedUser.name.equals(userName) && kickedUser.ip.equals(hostAddress)){
                                              finishThread();
                                              throw new UDPLibException("Kicked out from group!");
                                          }else{
                                              listeners.stream().forEach((listener) -> {
                                                    listener.userKicked(kickedUser);
                                              });
                                          }
                                      }
                                      break;
                case SERVER_IS_ALIVE_REQUEST: sendDatagram(serverAddress, Datagrams.createIsAliveResponseDatagram(encryptor));
                                          break;
                case CLIENT_UNICAST_MESSAGE:    user = findGroupUserbyInetAddr(source.getAddress());
                                            if(!(user != null && user.name.equals(userName) && user.ip.equals(hostAddress))){ //discard own messages
                                                addMessage(new StoredMessage(data, user, false));
                                            }
                                            break;
                case CLIENT_MULTICAST_MESSAGE:  user = findGroupUserbyInetAddr(source.getAddress());
                                            if(!(user != null && user.name.equals(userName) && user.ip.equals(hostAddress))){
                                                addMessage(new StoredMessage(data, user, true));
                                            }
                                            break;
                case SERVER_UNICAST_MESSAGE: addMessage(new StoredMessage(data, findGroupUserbyInetAddr(source.getAddress()), false));
                                            break;
                case SERVER_MULTICAST_MESSAGE: addMessage(new StoredMessage(data, findGroupUserbyInetAddr(source.getAddress()), true));
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
    public List<GroupUser> getCurrentGroupUsers() {
       LinkedList<GroupUser> groupUsersToReturn = new LinkedList<>();
       Collections.copy(groupUsersToReturn, groupUsers);

       return groupUsersToReturn;
    }

    /**
     * @param ip ip of user
     * @return user with given ip or null
     */
    private GroupUser findGroupUserbyInetAddr(InetAddress ip){
        for(GroupUser user: groupUsers){
            if(user.ip.equals(ip)){
                return user;
            }
        }
        return null;
    }
}