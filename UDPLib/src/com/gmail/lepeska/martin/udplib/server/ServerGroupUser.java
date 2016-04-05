package com.gmail.lepeska.martin.udplib.server;

import com.gmail.lepeska.martin.udplib.client.GroupUser;

/**
 * Extended for server, which also needs to store time of last ping request.
 * 
 * @author Martin Lepeška
 */
public class ServerGroupUser extends GroupUser{

    protected long lastPingTime = -1;
    
    /**
    * @param name Name of user  in group
    * @param ip IP of user
    */
    public ServerGroupUser(String name, String ip) {
        super(name, ip);
    }
    
    public void pingReceived(){
        pingToHost = System.currentTimeMillis() - lastPingTime;
    }
    
    public void pingSent(){
        lastPingTime = System.currentTimeMillis();
    }
    
}
