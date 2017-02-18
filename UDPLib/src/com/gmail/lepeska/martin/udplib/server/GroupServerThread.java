package com.gmail.lepeska.martin.udplib.server;

import com.gmail.lepeska.martin.udplib.util.ConfigLoader;
import com.gmail.lepeska.martin.udplib.util.Encryptor;
import com.gmail.lepeska.martin.udplib.AGroupThread;
import com.gmail.lepeska.martin.udplib.StoredMessage;
import com.gmail.lepeska.martin.udplib.UDPLibException;
import com.gmail.lepeska.martin.udplib.client.GroupUser;
import com.gmail.lepeska.martin.udplib.datagrams.ADatagram;
import com.gmail.lepeska.martin.udplib.datagrams.AccessResponseDatagram;
import com.gmail.lepeska.martin.udplib.datagrams.Datagrams;
import com.gmail.lepeska.martin.udplib.datagrams.IsAliveDatagram;
import com.gmail.lepeska.martin.udplib.datagrams.MessageDatagram;
import com.gmail.lepeska.martin.udplib.datagrams.UserDeadDatagram;
import com.gmail.lepeska.martin.udplib.datagrams.UserInfoDatagram;
import com.gmail.lepeska.martin.udplib.datagrams.explore.ExploreResponse;
import com.gmail.lepeska.martin.udplib.files.IServerShareListener;
import com.gmail.lepeska.martin.udplib.files.AServerSharedFile;
import com.gmail.lepeska.martin.udplib.files.ServerSharedBinaryFile;
import com.gmail.lepeska.martin.udplib.files.ServerSharedTextFile;
import java.io.File;
import java.net.DatagramPacket;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Martin Lepeška
 */
public class GroupServerThread extends AGroupThread{
     /**Users in group*/
    private final List<ServerGroupUser> groupUsers= Collections.synchronizedList(new LinkedList<>());
    /**This thread maintains info about group network*/
    private final ServerGroupInfoThread refreshThread;

    private final HashMap<String, AServerSharedFile> sharedFiles = new HashMap<>();
    
    /**
     * Creates new GroupServerThread bound on interface of given hostAddress with given password and group address.
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
    public GroupServerThread(String userName, String groupPassword, String hostAddress, String groupAddress, int port, int userInfoPeriod, int deadTime) throws UnknownHostException{
        Objects.requireNonNull(userName);
        this.userName = userName;
        this.groupPassword = groupPassword;
        this.hostAddress = InetAddress.getByName(hostAddress);
        this.groupAddress = InetAddress.getByName(groupAddress);
        this.port = port;
        this.refreshThread = new ServerGroupInfoThread(userInfoPeriod, deadTime);
        this.refreshThread.setDaemon(true);
        this.encryptor = (groupPassword!=null)?new Encryptor(groupPassword):new Encryptor();
        setDaemon(true);
    }
    
    /**
     * Creates important components, which cannot be created in constructor, before starting loop.
     */
    private void init(){
        try{
            //socket = new MulticastSocket(new InetSocketAddress(hostAddress, port));
            socket = new MulticastSocket(new InetSocketAddress(hostAddress, port));
            
            InetSocketAddress groupSocketAddr = new InetSocketAddress(groupAddress, port);
            
            if(hostAddress.getHostAddress().equals("0.0.0.0")){
                Logger.getLogger(ConfigLoader.class.getName()).log(Level.INFO, "Selected 0.0.0.0 -> detecting interfaces...");
               // socket.joinGroup(groupAddress);

                //be able to multicast on all posible interfaces
                //still lesser evil than switching interfaces on one socket
                Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
                for (NetworkInterface nic : Collections.list(nets)){
                    if(!nic.isLoopback() && nic.supportsMulticast() && nic.isUp()){
                        Enumeration<InetAddress> addresses = nic.getInetAddresses();
                        
                        while(addresses.hasMoreElements()){
                            InetAddress address = addresses.nextElement();
                            if(!address.getHostName().equals("0.0.0.0") && !address.isLinkLocalAddress() && !address.isLoopbackAddress() && !(address instanceof Inet6Address)){
                                MulticastSocket sendSocket = new MulticastSocket();
                                sendSocket.setNetworkInterface(nic);
                                sendSockets.add(sendSocket);
                                socket.joinGroup(new InetSocketAddress(groupAddress, port), nic);
                                Logger.getLogger(ConfigLoader.class.getName()).log(Level.INFO, "Bound on: {0} nic: {1}", new Object[]{address, nic});
                            }
                        }
                    }
                }
                
            }else{
                NetworkInterface nic = getInterfaceByIP(hostAddress.getHostAddress());
                socket.joinGroup(groupSocketAddr, nic);
                Logger.getLogger(ConfigLoader.class.getName()).log(Level.INFO, "Bound on: {0} nic: {1}", new Object[]{hostAddress, nic});
                sendSockets.add(socket);
            }
             
            refreshThread.setGroupServer(this);
            refreshThread.start();
            
            ServerGroupUser me = new ServerGroupUser(userName, hostAddress);
            groupUsers.add(me);
            listeners.stream().forEach((listener) -> {
                listener.joined(me);
            });
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
                byte[] buf = new byte[ADatagram.MAXIMUM_DATAGRAM_LENGTH];
                
                // receive request
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                ADatagram datagram = Datagrams.reconstructDatagram(encryptor, packet);

                if(datagram != null){
                        dealWithDatagram(packet, datagram);
                }else{
                        //trash    
                        Logger.getLogger(ConfigLoader.class.getName()).log(Level.WARNING, 
                                "Trash received: {0} -> {1} ", new String[]{Arrays.toString(buf), 
                                    Arrays.toString(encryptor.decrypt(buf))});
                }                
            }catch(Exception e){
                 Logger.getLogger(ConfigLoader.class.getName()).log(Level.SEVERE, 
                         "Error when parsing datagram! ", e);
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
        this.interrupt();
    }

    @Override
    public List<GroupUser> getCurrentGroupUsers() {
       LinkedList<GroupUser> groupUsersToReturn = new LinkedList<>();
       Collections.copy(groupUsersToReturn, groupUsers);

       return groupUsersToReturn;
    }
    
    /**
     * Called by ServerGroupInfoThread.
     * Request for all group members response,
     * anyone who will not respond in time,
     * is considered as dead.
     */
    void sendIsAliveRequests(){
        sendMulticastDatagram(new IsAliveDatagram(encryptor, true));
        
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
            
            if(user.pingsLeft() > ServerGroupUser.DEAD_COUNT){
                sendMulticastDatagram(new UserDeadDatagram(encryptor, user));
                
                listeners.stream().forEach((listener) -> {
                   listener.userKicked(user);
                });
                
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
            sendMulticastDatagram(new UserInfoDatagram(encryptor, user));
        });
    }

    @Override
    public void sendMessage(GroupUser target, String message) {
        ADatagram datagram = new MessageDatagram(encryptor, message, false, true);
        sendDatagram(target, datagram);
    }

    @Override
    public void sendMulticastMessage(String message) {
        ADatagram datagram = new MessageDatagram(encryptor, message, true, true);
        sendMulticastDatagram(datagram);
    }

    /**
     * @param file content to share
     * @param name unique id
     * @param listener object to notify about progress
     */
    public void shareFile(File file, String name, IServerShareListener listener){
        AServerSharedFile serverFile;
        
        if(file.getName().matches(ServerSharedTextFile.TEXT_FILES)){
            serverFile = new ServerSharedTextFile(file, name, this, encryptor, refreshThread.getDeadTime(), listener);
        }else{
            serverFile = new ServerSharedBinaryFile(file, name, this, encryptor, refreshThread.getDeadTime(), listener);
        }
        
        sharedFiles.put(name, serverFile);
        Thread sharing = new Thread(serverFile);
        sharing.setDaemon(true);
        sharing.start();
    }
    
    @Override
    protected void dealWithDatagram(DatagramPacket source, ADatagram datagram) {
        String[] messageSplit = datagram.getStringMessage();
        ServerGroupUser user;
        switch(datagram.getType()){
            case CLIENT_ACCESS_REQUEST:if(messageSplit.length >= 2 && (messageSplit[0].equals(groupPassword) || groupPassword==null)){
                                            ServerGroupUser newUser = new ServerGroupUser(messageSplit[1], source.getAddress());
                                            groupUsers.add(newUser);
                                            sendDatagram(source.getAddress(), new AccessResponseDatagram(encryptor, groupAddress, source.getAddress(), true));
                                            listeners.stream().forEach((listener) -> {
                                                listener.userJoined(newUser);
                                            });
                                        }else{
                                            sendDatagram(source.getAddress(), new AccessResponseDatagram(encryptor, hostAddress, source.getAddress(), false));
                                        }
                                        break;
            case CLIENT_IS_ALIVE_RESPONSE: user = findGroupUserbyInetAddr(source.getAddress());
                                            if(user != null) user.pingReceived();
                                            break;
            case CLIENT_UNICAST_MESSAGE:    user = findGroupUserbyInetAddr(source.getAddress());
                                            addMessage(new StoredMessage(Datagrams.reconstructMessage(messageSplit, 0), user, false));
                                            break;
            case CLIENT_MULTICAST_MESSAGE:  user = findGroupUserbyInetAddr(source.getAddress());
                                            addMessage(new StoredMessage(Datagrams.reconstructMessage(messageSplit, 0), user, true));
                                            break;
            case CLIENT_FILE_SHARE_PART_REQUEST: AServerSharedFile requested = sharedFiles.get(messageSplit[0]);
                                                    if(requested != null) requested.partRequest(Integer.parseInt(messageSplit[1]));
                                                    break;
            case CLIENT_EXPLORE_REQUEST: sendDatagram(source.getAddress(), new ExploreResponse(groupPassword != null));
                                         break;
        }
    }
    
    /**
     * @param ip ip of user
     * @return user with given ip or null
     */
    private ServerGroupUser findGroupUserbyInetAddr(InetAddress ip){
        for(ServerGroupUser gUser: groupUsers){
            if(gUser.ip.equals(ip)){
                return gUser;
            }
        }
        return null;
    }
}