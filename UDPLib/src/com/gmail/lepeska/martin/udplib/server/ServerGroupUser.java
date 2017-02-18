package com.gmail.lepeska.martin.udplib.server;

import com.gmail.lepeska.martin.udplib.client.GroupUser;
import java.net.InetAddress;

/**
 * Extended for server, which also needs to store time of last ping request.
 * 
 * @author Martin Lepeška
 */
public class ServerGroupUser extends GroupUser{
    /**Time stamp since last ping request to this user*/
    protected long lastPingTime = -1;
    
    /**How much pings in row can be left out*/
    public static final int DEAD_COUNT = 3;
    
    /**Counter of missed responses*/
    private int pingsLeft = 0;
    
    /**
    * @param name Name of user  in group
    * @param ip IP of user
    */
    public ServerGroupUser(String name, InetAddress ip) {
        super(name, ip);
    }
    /**
     * Used by GroupServerRunnable
     * Updates pingToHost value
     */
    synchronized void pingReceived(){
        pingToHost = System.currentTimeMillis() - lastPingTime;
        pingsLeft--;
    }
    
    /**
     * Used by GroupServerRunnable
     * Notifies about sending ping request(creates timestamp)
     */
    synchronized void pingSent(){
        lastPingTime = System.currentTimeMillis();
        pingsLeft++;
        
    }

    /**
     * Used by GroupServerRunnable
     * @return true, if user had not responded to last IS_ALIVE_REQUEST YET
     */
    public synchronized int pingsLeft() {
        return pingsLeft;
    }

    @Override
    public String toString() {
        return name+":"+ip.getHostName()+"     "+pingToHost+"ms"+"  W:"+pingsLeft;
    }
    
}