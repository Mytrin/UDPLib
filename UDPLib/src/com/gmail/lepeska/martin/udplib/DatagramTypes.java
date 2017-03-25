package com.gmail.lepeska.martin.udplib;

/**
 * List of all used datagrams used by UDPLib to manage group network.
 * 
 * @author Martin Lepeška
 */
public enum DatagramTypes {
    /**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*//**Sent by client to group, requesting all servers to report themselves*/
    CLIENT_EXPLORE_REQUEST(0, true),
    /**Sent by server as response to CLIENT_EXPLORE_REQUEST, contains info, if password is requested*/
    SERVER_EXPLORE_RESPONSE(1, true),
    /**Sent by client to server as request for adding to group users*/
    CLIENT_ACCESS_REQUEST(2, true),
    /**Sent by server as response to CLIENT_ACCESS_REQUEST, contains group address*/
    SERVER_ACCEPT_CLIENT_RESPONSE(3, true),
    
    /**Periodically sent by server thread. Clients, which won't respond three times are considered dead*/
    SERVER_IS_ALIVE_REQUEST(4, true),
    /**Response to SERVER_IS_ALIVE_REQUEST, so client is not kicked from group*/
    CLIENT_IS_ALIVE_RESPONSE(5, true),
    /**Periodically sent by server thread, contains info about current state of group*/
    SERVER_CLIENTS_INFO(6, true),
    /**Sent by server thread, warns about kicked user*/
    SERVER_CLIENT_DEAD(7, true),
    
    SERVER_UNICAST_MESSAGE(8, true),
    SERVER_MULTICAST_MESSAGE(9, true),
    CLIENT_UNICAST_MESSAGE(10, true),
    CLIENT_MULTICAST_MESSAGE(11, true),
    
    /**Sent by sharing thread  automatically or on request by client*/
    TEXT_FILE_SHARE_PART(12, true),
    /**Sent by sharing thread  automatically after sending all FILE_SHARE_PART, 
     * client may request resending some of them*/
    FILE_SHARE_FINISH(13, true),
    /**Sent by receiving thread after FILE_RECEIVE_FINISH to obtain missing FILE_SHARE_PART*/
    FILE_SHARE_PART_REQUEST(14, true),
    /**Sent by sharing thread automatically or on request by client*/
    BINARY_FILE_SHARE_PART(15, false),
    
    TRASH(-1, false),
    THE_ANSWER(42, false); // :-)
    ;
    
    /** byte, by which is this type represented in datagram content*/
    public final int index;
    /** true, if datagram data are at String format*/
    public final boolean isString;
    
    /**
     * @param index byte, by which is this type represented in datagram content
     * @param isString true, if datagram data are at String format
     */
    private DatagramTypes(int index, boolean isString){
        this.index = index;
        this.isString = isString;
    }
    
    /**Better than calling DatagramTypes.values() all the time*/
    private static final DatagramTypes[] VALUES = DatagramTypes.values();
    
    /**
     * @param index datagram type
     * @return DatagramType on this index or TRASH
     */
    public static DatagramTypes getTypeByIndex(int index){
        if(index < VALUES.length -1 && index > -1){
            return VALUES[index];
        }
        return TRASH;   
    }
    
}