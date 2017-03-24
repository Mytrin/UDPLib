package com.gmail.lepeska.martin.udplib.server;

import com.gmail.lepeska.martin.udplib.AGroupNetwork;
import com.gmail.lepeska.martin.udplib.util.ConfigLoader;
import com.gmail.lepeska.martin.udplib.files.IServerShareListener;
import java.io.File;
import java.net.UnknownHostException;
import java.util.Objects;

/**
 * AGroupNetwork implementation, which creates new group network, where user becomes "server."
 * 
 * @author Martin Lepeška
 */
public class ServerGroupNetwork extends AGroupNetwork{

    /**
     * Prepares new GroupServerThread bound on interface of given hostAddress with given password and group address.
     * 
     * @param userName User's name in network
     * @param groupPassword Password required to access this group or null, if none
     * @param hostAddress Address in network interface, which should server socket use
     * @param groupAddress Address of used multi cast group
     * @param port Port of server socket
     * @param userInfoPeriod Time between server sends info about group users and requests response from other users (ms)
     * @param deadTime Time, which will server wait after sending request, before it announces user as dead (ms)
     * @throws java.net.UnknownHostException
     */
    public ServerGroupNetwork(String userName, String groupPassword, String hostAddress, 
            String groupAddress, int port, int userInfoPeriod, int deadTime) throws UnknownHostException {
            this.groupThread = new GroupServerThread(userName, groupPassword, hostAddress, 
                    groupAddress, port, userInfoPeriod, deadTime);
    }
    
        
    /**
     * Prepares new GroupServerThread bound on interface of given hostAddress with given password and default group address loaded from configuration file.
     * 
     * @param userName User's name in network
     * @param groupPassword Password required to access this group or null, if none
     * @param hostAddress Address in network interface, which should server socket use
     * @param groupAddress Address of used multi cast group
     * @param port Port of server socket
     * @throws UnknownHostException 
     */
    public ServerGroupNetwork(String userName, String groupPassword, String hostAddress, String groupAddress, int port) throws UnknownHostException{
        this(userName, groupPassword, hostAddress, groupAddress, port, 
                ConfigLoader.getInt("user-info-period", 5000), ConfigLoader.getInt("dead-time", 2000));
    }

    /**
     * Prepares new GroupServerThread on interface of given hostAddress with given password and default group address loaded from configuration file.
     * 
     * @param userName User's name in network
     * @param groupPassword Password required to access this group or null, if none
     * @param hostAddress Address in network interface, which should server socket use
     * @param port Port of server socket
     * @throws UnknownHostException 
     */
    public ServerGroupNetwork(String userName, String groupPassword, String hostAddress, int port) throws UnknownHostException{
        this(userName, groupPassword, hostAddress, ConfigLoader.getString("default-group", "225.226.227.228"), port);
    }

    /**
     * Prepares new GroupServerThread with given password and default values loaded from configuration file.
     * 
     * @param userName User's name in network
     * @param groupPassword Password required to access this group or null, if none
     * @throws UnknownHostException 
     */
    public ServerGroupNetwork(String userName, String groupPassword) throws UnknownHostException{
        this(userName, groupPassword, ConfigLoader.getString("default-server-ip", "0.0.0.0"), 
                ConfigLoader.getString("default-group", "225.226.227.228"), ConfigLoader.getInt("default-port", 52511));
    }
    
    /**
     * Prepares new GroupServerThread with no required password and default values loaded from configuration file.
     * 
     * @param userName User's name in network
     * @throws UnknownHostException 
     */
    public ServerGroupNetwork(String userName) throws UnknownHostException{
        this(userName, null, ConfigLoader.getString("default-server-ip", "0.0.0.0"), 
                ConfigLoader.getString("default-group", "225.226.227.228"), ConfigLoader.getInt("default-port", 52511));
    }

    /**
     * Sends content of file into group, to share it with other client.
     * 
     * @param file content to share
     * @param name unique id
     * @param listener object to notify about progress
     */
    public void shareFile(File file, String name, IServerShareListener listener){
       Objects.requireNonNull(file);
       ((GroupServerThread)groupThread).shareFile(file, name, listener);
    }
    
}