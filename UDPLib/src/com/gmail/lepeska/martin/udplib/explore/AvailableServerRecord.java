package com.gmail.lepeska.martin.udplib.explore;

import java.net.InetAddress;

/**
 *
 * Class storing data received from ExploreRunnable
 * 
 * @author Martin Lepeška
 */
public class AvailableServerRecord {
    /**Group address, in which is server operating*/
    public final InetAddress groupAddress;
    /**Used port*/
    public final int port;
    /**IP address of server*/
    public final InetAddress server;
    /**True, if you need password for access*/
    public final boolean requiresPassword;

    /**
     * 
     * @param groupAddress Group address, in which is server operating
     * @param port Used port
     * @param server IP address of server
     * @param requiresPassword True, if you need password for access
     */
    public AvailableServerRecord(InetAddress groupAddress, int port, InetAddress server, boolean requiresPassword) {
        this.groupAddress = groupAddress;
        this.port = port;
        this.server = server;
        this.requiresPassword = requiresPassword;
    }

    @Override
    public String toString() {
        return server+":"+port+(requiresPassword?"  P":"");
    }
    
    
}