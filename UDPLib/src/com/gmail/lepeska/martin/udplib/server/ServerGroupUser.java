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
    
    /**True, if user already missed one IS_ALIVE request*/
    protected boolean couldBeDead = false;
    /**True, if server is waiting for IS_ALIVE response from this user*/
    protected boolean waitingForResponse = false;
    
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
         waitingForResponse = false;
         couldBeDead = false;
    }
    
    /**
     * Used by GroupServerRunnable
     * Notifies about sending ping request(creates timestamp)
     */
    synchronized void pingSent(){
        lastPingTime = System.currentTimeMillis();
        if(!waitingForResponse){
            waitingForResponse = true;
        }else{
            couldBeDead = true;
        }
        
    }

    /**
     * Used by GroupServerRunnable
     * @return true, if user had not responded to last IS_ALIVE_REQUEST YET
     */
    public synchronized boolean waitingForResponse() {
        return waitingForResponse;
    }
    
    /**
     * Used by GroupServerRunnable
     * @return true, if user had not responded to previous IS_ALIVE_REQUEST
     */
    public synchronized boolean couldBeDead() {
        return couldBeDead;
    }

    @Override
    public String toString() {
        return name+":"+ip.getHostName()+"     "+pingToHost+"ms"+"  W:"+waitingForResponse;
    }
    
}