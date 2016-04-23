package com.gmail.lepeska.martin.udplib.server;

import com.gmail.lepeska.martin.udplib.ConfigLoader;
import com.gmail.lepeska.martin.udplib.DatagramTypes;
import com.gmail.lepeska.martin.udplib.Datagrams;
import com.gmail.lepeska.martin.udplib.Encryptor;
import com.gmail.lepeska.martin.udplib.IGroupRunnable;
import com.gmail.lepeska.martin.udplib.StoredMessage;
import com.gmail.lepeska.martin.udplib.UDPLibException;
import com.gmail.lepeska.martin.udplib.client.GroupUser;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Martin Lepeška
 */
public class GroupServerRunnable extends IGroupRunnable{
     /**Users in group*/
    private final ArrayList<ServerGroupUser> groupUsers= new ArrayList<>();
    /**This thread maintains info about group network*/
    private final ServerGroupInfoThread refreshThread;
    
    public static void main(String[] args) throws UnknownHostException{
        if(ConfigLoader.loadConfig()){
            new Thread(new GroupServerRunnable("Server")).start();
           //  new Thread(new GroupServerRunnable("Server", null, "127.0.0.1", 8080)).start();
        }
    }
    
    /**
     * Creates new GroupServerRunnable bound on interface of given hostAddress with given password and group address.
     * 
     * @param userName User's name in network
     * @param groupPassword Password required to access this group or null, if none
     * @param hostAddress Address in network interface, which should server socket use
     * @param groupAddress Address of used multi cast group
     * @param port Port of server socket
     * @param userInfoPeriod Time between server sends info about group users and requests response from other users (ms)
     * @param deadTime Time, which will server wait after sending request, before it announces user as dead (ms)
     * @throws UnknownHostException 
     */
    public GroupServerRunnable(String userName, String groupPassword, String hostAddress, String groupAddress, int port, int userInfoPeriod, int deadTime) throws UnknownHostException{
        Objects.requireNonNull(userName);
        this.groupUsers.add(new ServerGroupUser(userName, InetAddress.getByName(hostAddress)));
        this.userName = userName;
        this.groupPassword = groupPassword;
        this.hostAddress = InetAddress.getByName(hostAddress);
        this.groupAddress = InetAddress.getByName(groupAddress);
        this.port = port;
        this.refreshThread = new ServerGroupInfoThread(userInfoPeriod, deadTime);
        this.refreshThread.setDaemon(true);
        this.encryptor = (groupPassword!=null)?new Encryptor(groupPassword):new Encryptor();
    }
    
    /**
     * Creates new GroupServerRunnable bound on interface of given hostAddress with given password and default group address loaded from configuration file.
     * 
     * @param userName User's name in network
     * @param groupPassword Password required to access this group or null, if none
     * @param hostAddress Address in network interface, which should server socket use
     * @param groupAddress Address of used multi cast group
     * @param port Port of server socket
     * @throws UnknownHostException 
     */
    public GroupServerRunnable(String userName, String groupPassword, String hostAddress, String groupAddress, int port) throws UnknownHostException{
        this(userName, groupPassword, hostAddress, groupAddress, port, ConfigLoader.getInt("user-info-period"), ConfigLoader.getInt("dead-time"));
    }

    /**
     * Creates new GroupServerRunnable bound on interface of given hostAddress with given password and default group address loaded from configuration file.
     * 
     * @param userName User's name in network
     * @param groupPassword Password required to access this group or null, if none
     * @param hostAddress Address in network interface, which should server socket use
     * @param port Port of server socket
     * @throws UnknownHostException 
     */
    public GroupServerRunnable(String userName, String groupPassword, String hostAddress, int port) throws UnknownHostException{
        this(userName, groupPassword, hostAddress, ConfigLoader.getString("default-group"), port);
    }

    /**
     * Creates new GroupServerRunnable with given password and default values loaded from configuration file.
     * 
     * @param userName User's name in network
     * @param groupPassword Password required to access this group or null, if none
     * @throws UnknownHostException 
     */
    public GroupServerRunnable(String userName, String groupPassword) throws UnknownHostException{
        this(userName, groupPassword, ConfigLoader.getString("default-server-ip"), ConfigLoader.getString("default-group"), ConfigLoader.getInt("default-port"));
    }
    
    /**
     * Creates new GroupServerRunnable with no required password and default values loaded from configuration file.
     * 
     * @param userName User's name in network
     * @throws UnknownHostException 
     */
    public GroupServerRunnable(String userName) throws UnknownHostException{
        this(userName, null, ConfigLoader.getString("default-server-ip"), ConfigLoader.getString("default-group"), ConfigLoader.getInt("default-port"));
    }
    
    /**
     * Creates important components, which could not been created in constructor, before starting loop.
     */
    private void init(){
        try{
            socket = new MulticastSocket(new InetSocketAddress(hostAddress, port));
            socket.joinGroup(groupAddress);
            
            refreshThread.setGroupServer(this);
            refreshThread.start();
            
        }catch(Exception e){
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
                
                
                // receive request
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                
                DatagramTypes type = Datagrams.getDatagramType(buf);

                if(type == DatagramTypes.CLIENT_EXPLORE_REQUEST){
                    sendDatagram(packet.getAddress(), Datagrams.createExploreResponse(groupPassword != null));
                }else{
                    decryptedBuf = encryptor.decrypt(buf);
                    type = Datagrams.getDatagramType(decryptedBuf);
                    
                    System.out.println("-> "+new String(decryptedBuf).trim()+" type: "+type.name());
                    
                    if(type != DatagramTypes.TRASH){
                        dealWithPacket(packet, type, Datagrams.unpack(decryptedBuf));
                    }else{
                        //trash    
                        Logger.getLogger(ConfigLoader.class.getName()).log(Level.WARNING, "Trash received: {0} -> {1} ", new String[]{new String(buf), new String(decryptedBuf)});
                    }
                }
                
            }catch(Exception e){
                 Logger.getLogger(ConfigLoader.class.getName()).log(Level.SEVERE, "Error when parsing datagram! ", e);
            }
        }

        finishThread();
    }

    @Override
    protected void finishThread() {
        super.finishThread();
        
        if(refreshThread != null){
            refreshThread.interrupt();
        }
    }

    @Override
    public void leave() {
        Thread.currentThread().interrupt();
    }

    @Override
    public GroupUser[] getGroupUsers() {
        return (GroupUser[])groupUsers.toArray();
    }
    
    /**
     * Called by ServerGroupInfoThread.
     * Request for all group members response,
     * anyone who will not respond in time,
     * is considered as dead.
     */
    void sendIsAliveRequests(){
        byte[] data = Datagrams.createIsAliveRequestDatagram(encryptor);
        sendMulticastDatagram(data);
        
        groupUsers.stream().forEach((user) -> {
            user.pingSent();
            
            if(user.name.equals(userName) && user.ip.equals(hostAddress)){
                user.pingReceived();
            }
        });
    }
    
    /**
     * Called by ServerGroupInfoThread.
     * Removes any presumably dead users.
     */
    void killDead(){
        Iterator<ServerGroupUser> userIt = groupUsers.iterator();
        
        while(userIt.hasNext()){
            ServerGroupUser user = userIt.next();
            
            if(user.waitingForResponse() && user.couldBeDead()){
                byte[] data = Datagrams.createUserDeadDatagram(encryptor, user);
                sendMulticastDatagram(data);
                
                userIt.remove();
            }
        }
    }
    
    /**
     * Called by ServerGroupInfoThread.
     * Announces current group state to all group users.
     */
    void sendInfo(){
        groupUsers.stream().forEach((user) -> {
            byte[] data = Datagrams.createUserInfoDatagram(encryptor, user);
            sendMulticastDatagram(data);
        });
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
    protected void dealWithPacket(DatagramPacket source, DatagramTypes type, byte[] data) {
        String[] messageSplit = new String(data).trim().split(Datagrams.DELIMITER);
        ServerGroupUser user;
        
        switch(type){
            case CLIENT_ACCESS_REQUEST:if(messageSplit.length >= 2 && messageSplit[0].equals(groupPassword)){
                                            groupUsers.add(new ServerGroupUser(messageSplit[1], source.getAddress()));
                                            sendDatagram(source.getAddress(), Datagrams.createServerAcceptClientResponse(encryptor, groupAddress, true));
                                        }else{
                                            sendDatagram(source.getAddress(), Datagrams.createServerAcceptClientResponse(encryptor, hostAddress, false));
                                        }
                                        break;
            case CLIENT_IS_ALIVE_RESPONSE: user = findGroupUserbyInetAddr(source.getAddress());
                                            if(user != null) user.pingReceived();
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
    }
    
    private ServerGroupUser findGroupUserbyInetAddr(InetAddress ip){
        for(ServerGroupUser user: groupUsers){
            if(user.ip.equals(ip)){
                return user;
            }
        }
        return null;
    }
    
    private ServerGroupUser findGroupUserbyName(String userName){
        for(ServerGroupUser user: groupUsers){
            if(user.name.equals(userName)){
                return user;
            }
        }
        return null;
    }
    
}