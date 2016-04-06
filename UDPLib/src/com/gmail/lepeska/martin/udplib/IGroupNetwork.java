package com.gmail.lepeska.martin.udplib;

/**
 * This interface serves as cover over UDP multicast communication.
 * It enables to easily send messages and files to other clients.
 * There are two types of GroupNetwork - HostGroupNetwork and ClientGroupNetwork 
 * Clients usually send all important messages to host, who sends them to all clients.
 * Host keeps and updates info about network, its users and their "ping" to him.
 *  
 * Group IP(224.0.0.0 to 239.255.255.255)
 * 
 * @author Martin Lepeška
 */
public interface IGroupNetwork {
    
}
