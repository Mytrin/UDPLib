package com.gmail.lepeska.martin.udplib.server;

import com.gmail.lepeska.martin.udplib.UDPLibException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This thread is responsible for maintaining info about current group users.
 * GroupServer has to be set before start, so the thread can use it to send datagrams!
 *
 * @author Martin Lepeška
 */
public class ServerGroupInfoThread extends Thread{

    /**Time between server sending info about group users and requesting response from other users (ms)*/
    private final AtomicInteger userInfoPeriod;
    /**Time, which will server wait after sending request, before it announces user as dead (ms)*/
    private final AtomicInteger deadTime;
    /**Thread responsible for sending datagrams to group*/
    private GroupServerThread groupServer;
    
    /**
     * 
     * @param userInfoPeriod Time between server sending info about group users and requesting response from other users (ms)
     * @param deadTime Time, which will server wait after sending request, before it announces user as dead (ms)
     */
    public ServerGroupInfoThread(int userInfoPeriod, int deadTime) {
        super("ServerGroupInfoThread");
        this.userInfoPeriod = new AtomicInteger(userInfoPeriod);
        this.deadTime = new AtomicInteger(deadTime);
    }

    @Override
    public void run() {
        if(groupServer == null){
            throw new UDPLibException("Started ServerGroupInfoThread while groupThread value was not set!");
        }

        while(!isInterrupted()){
            try{
                Thread.sleep(userInfoPeriod.get());
                groupServer.sendInfo();
                
                groupServer.sendIsAliveRequests();
                Thread.sleep(deadTime.get());
                groupServer.killDead();
            }catch(InterruptedException ex){
                 Logger.getLogger(GroupServerThread.class.getName()).log(Level.WARNING, "Shutting down ServerGroupInfoThread...", ex);
                 interrupt();
            }catch(Exception e){
                throw new UDPLibException("ServerGroupInfoThread loop problem: ", e);
            }
        }
    }

    /**
     * Has to be called before start!
     * @param groupServer Thread responsible for sending datagrams to group
     */
    public void setGroupServer(GroupServerThread groupServer) {
        this.groupServer = groupServer;
    }
    
    /**
     * @param userInfoPeriod Time between server sending info about group users and requesting response from other users (ms)
     */
    public void setUserInfoPeriod(int userInfoPeriod) {
        this.userInfoPeriod.set(userInfoPeriod);
    }

    /**
     * @param deadTime Time, which will server wait after sending request, before it announces user as dead (ms)
     */
    public void setDeadTime(int deadTime) {
        this.deadTime.set(deadTime);
    }
}