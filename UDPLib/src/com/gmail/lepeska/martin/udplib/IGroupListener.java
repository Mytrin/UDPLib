
package com.gmail.lepeska.martin.udplib;

import com.gmail.lepeska.martin.udplib.client.GroupUser;

/**
 *
 * Implementations of this class receive events from AGroupThread and deal with them in way specified by user.
 * 
 * @author Martin Lepeška
 */
public interface IGroupListener {
    
    /**
     * Called on successful join(or creation) to Network
     * @param me how am I represented to other members of network
     */
    public void joined(GroupUser me);
    
    /**
     * Called when client is notified about loss of GroupUser
     * @param who lost group user
     */
    public void userKicked(GroupUser who);
    
    /**
     * Called when client detects new GroupUser
     * @param who new group user
     */
    public void userJoined(GroupUser who);
    
    /**
     * Called when thread received new message
     */
    public void mesageReceived();
    
    /**
     * Called when client had to leave network
     */
    public void kicked();
}