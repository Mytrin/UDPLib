package com.gmail.lepeska.martin.udplib.server;

import com.gmail.lepeska.martin.udplib.client.GroupUser;

/**
 * Extended for server, which also needs to store time of last ping request.
 * 
 * @author Martin Lepeška
 */
public class ServerGroupUser extends GroupUser{
    /**Time stamp since last ping request to this user*/
    protected long lastPingTime = -1;
    
    
    /**
    * @param name Name of user  in group
    * @param ip IP of user
    */
    public ServerGroupUser(String name, String ip) {
        super(name, ip);
    }
    /**
     * Updates pingToHost value
     */
    public void pingReceived(){
        pingToHost = System.currentTimeMillis() - lastPingTime;
    }
    
    /**
     * Notifies about sending ping request(creates timestamp)
     */
    public void pingSent(){
        lastPingTime = System.currentTimeMillis();
    }
    
}