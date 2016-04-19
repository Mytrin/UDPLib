package com.gmail.lepeska.martin.udplib.server;

import com.gmail.lepeska.martin.udplib.ConfigLoader;
import com.gmail.lepeska.martin.udplib.GroupRunnable;
import com.gmail.lepeska.martin.udplib.client.GroupUser;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Objects;

/**
 *
 * @author mytrin
 */
public class GroupServerRunnable extends GroupRunnable{
     /**Users, which already responded on ping*/
    private final ArrayList<ServerGroupUser> groupUsers= new ArrayList<>();
    /**This thread maintains info about group network*/
    private final Thread refreshThread;
    
    public static void main(String[] args) throws UnknownHostException{
        if(ConfigLoader.loadConfig()){
           // new Thread(new GroupServerRunnable("Server")).start();
             new Thread(new GroupServerRunnable("Server", null, "127.0.0.1", 8080)).start();
        }
    }
    
    /**
     * Creates new GroupServerRunnable bound on interface of given hostAddress with given password and default group address loaded from configuration file.
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
        this.userName = userName;
        this.groupPassword = groupPassword;
        this.hostAddress = InetAddress.getByName(hostAddress);
        this.groupAddress = InetAddress.getByName(groupAddress);
        this.port = port;
        this.refreshThread = new Thread(new ServerGroupInfoThread(userInfoPeriod, deadTime));
        this.refreshThread.setDaemon(true);
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
        
    @Override
    public void run() {
        try{
            socket = new MulticastSocket(new InetSocketAddress(hostAddress, port));
            socket.joinGroup(groupAddress);
            
            refreshThread.start();
            
            //while(!Thread.currentThread().isInterrupted()){
                //TODO receive datagram
            //}
            
            finishThread();
            
            System.out.println("FINISH");
        }catch(Exception e){
            System.err.println(e);
        }
    }

    @Override
    protected void finishThread() {
        super.finishThread();
        refreshThread.interrupt();
    }

    @Override
    public void leave() {
        Thread.currentThread().interrupt();
    }

    @Override
    public GroupUser[] getGroupUsers() {
        return (GroupUser[])groupUsers.toArray();
    }
    
    void sendIsAliveRequests(){
        
    }
    
}