package com.gmail.lepeska.martin.udplib;

/**
 * List of all used datagrams used by UDPLib to manage group network.
 * 
 * @author Martin Lepeška
 */
public enum DatagramTypes {
    /**Sent by client to group, requesting all servers to report themselves*/
    CLIENT_EXPLORE_REQUEST((byte)0),
    /**Sent by server as response to CLIENT_EXPLORE_REQUEST, contains info, if password is requested*/
    SERVER_EXPLORE_RESPONSE((byte)1),
    /**Sent by client to server as request for adding to group users*/
    CLIENT_ACCESS_REQUEST((byte)2),
    /**Sent by server as response to CLIENT_ACCESS_REQUEST, contains group address*/
    SERVER_ACCEPT_CLIENT_RESPONSE((byte)3),
    
    /**Periodically sent by server thread. Clients, which won't respond three times are considered dead*/
    SERVER_IS_ALIVE_REQUEST((byte)4),
    /**Response to SERVER_IS_ALIVE_REQUEST, so client is not kicked from group*/
    CLIENT_IS_ALIVE_RESPONSE((byte)5),
    /**Periodically sent by server thread, contains info about current state of group*/
    SERVER_CLIENTS_INFO((byte)6),
    /**Sent by server thread, warns about kicked user*/
    SERVER_CLIENT_DEAD((byte)7),
    
    SERVER_UNICAST_MESSAGE((byte)8),
    SERVER_MULTICAST_MESSAGE((byte)9),
    CLIENT_UNICAST_MESSAGE((byte)10),
    CLIENT_MULTICAST_MESSAGE((byte)11),
    
    TRASH((byte)-1);
    ;
    
    /** byte, by which is this type represented in datagram content*/
    public final byte index;
    
    /**
     * @param index byte, by which is this type represented in datagram content
     */
    private DatagramTypes(byte index){
        this.index = index;
    }
    
    /**Better than calling DatagramTypes.values() all the time*/
    private static final DatagramTypes[] VALUES = DatagramTypes.values();
    
    /**
     * @param index datagram type
     * @return DatagramType on this index or TRASH
     */
    public static DatagramTypes getTypeByIndex(byte index){
        if(index < VALUES.length){
            return VALUES[index];
        }
        return TRASH;   
    }
}