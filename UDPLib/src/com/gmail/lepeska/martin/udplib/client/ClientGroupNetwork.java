package com.gmail.lepeska.martin.udplib.client;

import com.gmail.lepeska.martin.udplib.AGroupNetwork;
import com.gmail.lepeska.martin.udplib.util.ConfigLoader;
import java.net.UnknownHostException;

/**
 * AGroupNetwork implementation, which tries to join existing group network, where user becomes "client."
 * 
 * @author Martin Lepeška
 */
public class ClientGroupNetwork extends AGroupNetwork{
    /**
     * Prepares new GroupClientThread bound on interface of given hostAddress with given password and server address.
     * 
     * @param userName User's name in network
     * @param groupPassword Password required to access this group or null, if none
     * @param serverAddress Address of group owner
     * @param port Port of server socket
     * @throws UnknownHostException 
     */
    public ClientGroupNetwork(String userName, String groupPassword, String serverAddress, int port) throws UnknownHostException{
        groupThread = new GroupClientThread(userName, groupPassword, serverAddress, port);
    }
    
    /**
     *  Prepares new GroupClientThread with given password and default values loaded from configuration file.
     * 
     * @param userName User's name in network
     * @param groupPassword Password required to access this group or null, if none
     * @param serverAddress Address of group owner
     * @throws UnknownHostException 
     */
    public ClientGroupNetwork(String userName, String groupPassword, String serverAddress) throws UnknownHostException{
        this(userName, groupPassword, serverAddress, ConfigLoader.getInt("default-port", 52511));
    }
}