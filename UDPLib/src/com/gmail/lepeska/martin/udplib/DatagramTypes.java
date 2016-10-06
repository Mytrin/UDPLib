package com.gmail.lepeska.martin.udplib;

/**
 * List of all used datagrams used by UDPLib to manage group network.
 * 
 * @author Martin Lepeška
 */
public enum DatagramTypes {
    /**Sent by client to group, requesting all servers to report themselves*/
    CLIENT_EXPLORE_REQUEST(0),
    /**Sent by server as response to CLIENT_EXPLORE_REQUEST, contains info, if password is requested*/
    SERVER_EXPLORE_RESPONSE(1),
    /**Sent by client to server as request for adding to group users*/
    CLIENT_ACCESS_REQUEST(2),
    /**Sent by server as response to CLIENT_ACCESS_REQUEST, contains group address*/
    SERVER_ACCEPT_CLIENT_RESPONSE(3),
    
    /**Periodically sent by server thread. Clients, which won't respond three times are considered dead*/
    SERVER_IS_ALIVE_REQUEST(4),
    /**Response to SERVER_IS_ALIVE_REQUEST, so client is not kicked from group*/
    CLIENT_IS_ALIVE_RESPONSE(5),
    /**Periodically sent by server thread, contains info about current state of group*/
    SERVER_CLIENTS_INFO(6),
    /**Sent by server thread, warns about kicked user*/
    SERVER_CLIENT_DEAD(7),
    
    SERVER_UNICAST_MESSAGE(8),
    SERVER_MULTICAST_MESSAGE(9),
    CLIENT_UNICAST_MESSAGE(10),
    CLIENT_MULTICAST_MESSAGE(11),
    
    /**Sent by server automatically or on request by client*/
    SERVER_FILE_SHARE_PART(12),
    /**Sent by server automatically after sending all SERVER_FILE_SHARE_PARTs, 
     * client may request resending some of them*/
    SERVER_FILE_SHARE_FINISH(13),
    /**Sent by client after SERVER_FILE_SHARE_FINISH to obtain missing SERVER_FILE_SHARE_PARTs*/
    CLIENT_FILE_SHARE_PART_REQUEST(14),
    
    TRASH(-1),
    THE_ANSWER(42);
    ;
    
    /** byte, by which is this type represented in datagram content*/
    public final int index;
    
    /**
     * @param index byte, by which is this type represented in datagram content
     */
    private DatagramTypes(int index){
        this.index = index;
    }
    
    /**Better than calling DatagramTypes.values() all the time*/
    private static final DatagramTypes[] VALUES = DatagramTypes.values();
    
    /**
     * @param index datagram type
     * @return DatagramType on this index or TRASH
     */
    public static DatagramTypes getTypeByIndex(int index){
        if(index < VALUES.length && index > -1){
            return VALUES[index];
        }
        return TRASH;   
    }
    
}