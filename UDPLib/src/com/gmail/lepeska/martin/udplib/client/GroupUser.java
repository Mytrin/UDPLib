package com.gmail.lepeska.martin.udplib.client;

import java.net.InetAddress;


/**
 * Class for storing info about users of network.
 * 
 * @author Martin Lepeška
 */
public class GroupUser {
   /**Initial value of pingToHost*/
   public static final int PING_UNKNOWN = -1;
    /**Time, which it takes to packet from this user to reach Host*/
   protected long pingToHost=PING_UNKNOWN; //-1 = unknown yet
   /**IP of user*/
   public InetAddress ip;
   /**Name of user  in group*/
   public String name;

   /**
    * @param name Name of user  in group
    * @param ip IP of user
    */
    public GroupUser(String name, InetAddress ip){
        this.name=name;
        this.ip=ip;
    }

    /**
     * @return Time, which it takes to packet from this user to reach Host
     */
    public long getPingToHost() {
        return pingToHost;
    }

    /**
     * Used by GroupClientRunnable
     * @param pingToHost Time, which it takes to packet from this user to reach Host
     */
    void setPingToHost(long pingToHost) {
        this.pingToHost = pingToHost;
    }

    @Override
    public String toString() {
        return name+":"+ip.getHostName()+"     "+pingToHost+"ms";
    }

}