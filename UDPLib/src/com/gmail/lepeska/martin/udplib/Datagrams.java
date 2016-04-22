package com.gmail.lepeska.martin.udplib;

import com.gmail.lepeska.martin.udplib.client.GroupUser;
import com.gmail.lepeska.martin.udplib.server.GroupServerRunnable;


/**
 * Singleton designed for creating datagrams,
 * GroupThreads are already long enough.
 * 
 * DATAGRAM CONTENT(bytes): DATAGRAM_HEADER + DATAGRAM TYPE + message
 * MESSAGE CONTENT(string): value1|value2|value3...
 * 
 * @author Martin Lepeška
 */
public class Datagrams {
    /**Maximal safe datagram length*/
    public static final int MAXIMUM_DATA_LENGTH = 512;
    /**Start sequence of bytes, which identifies, that datagram is sent by UDPLib*/
    public static final byte[] DATAGRAM_HEADER = ("UDPL").getBytes();
    
    private Datagrams() {}
    
    /**Compares length of created datagram with MAXIMUM_DATA_LENGTH and throws an error, in case it exceeded.*/
    private static void validateLength(int length){
        if(length >MAXIMUM_DATA_LENGTH){
            throw new UDPLibException("Message is larger than"+MAXIMUM_DATA_LENGTH+" bytes!");
        }
    }
    
    /**
     * Creates UDPLib message datagram with given type. Private because of safety(messageType shouldn't be exposed)
     * @param encryptor Encryptor of sending thread
     * @param message data to be sent
     * @param messageType identifies type of packet and role of sender
     * @return data of UDPLib message datagram
     */
    private static byte[] createDatagram(Encryptor encryptor, String message, DatagramTypes messageType){
        //I know it's ugly, just haven't find better way so far...
        byte[] messageBytes = encryptor.encrypt((((char)messageType.index)+message).getBytes());
        byte[] toReturn = new byte[DATAGRAM_HEADER.length+messageBytes.length];
        
        validateLength(toReturn.length);
        
        System.arraycopy(DATAGRAM_HEADER, 0, toReturn, 0, DATAGRAM_HEADER.length);
        System.arraycopy(messageBytes, 0, toReturn, DATAGRAM_HEADER.length, messageBytes.length);
        
        return toReturn;
    }
    
    /**
     * Creates UDPLib message datagram data.
     * @param encryptor Encryptor of sending thread
     * @param message data to be sent
     * @param thread identifies role of sender
     * @param isMulticast true if message is to be sent to all group users
     * @return data of UDPLib message datagram
     */
    public static byte[] createMessageDatagram(Encryptor encryptor, String message, boolean isMulticast, IGroupRunnable thread){
        DatagramTypes type;
        
        if(thread instanceof GroupServerRunnable){
            type = isMulticast?DatagramTypes.SERVER_MULTICAST_MESSAGE:DatagramTypes.SERVER_UNICAST_MESSAGE;
        }else{
            type = isMulticast?DatagramTypes.CLIENT_MULTICAST_MESSAGE:DatagramTypes.CLIENT_UNICAST_MESSAGE;
        }
                
        return createDatagram(encryptor, message, type);
    }
    
    /**
     * @param encryptor  Encryptor of sending thread
     * @return data of UDPLib SERVER_IS_ALIVE_REQUEST datagram
     */
    public static byte[] createIsAliveRequestDatagram(Encryptor encryptor){
        return createDatagram(encryptor, "", DatagramTypes.SERVER_IS_ALIVE_REQUEST);
    }
    
    /**
     * @param encryptor  Encryptor of sending thread
     * @return data of UDPLib CLIENT_IS_ALIVE_RESPONSE datagram
     */
    public static byte[] createIsAliveResponseDatagram(Encryptor encryptor){
        return createDatagram(encryptor, "", DatagramTypes.CLIENT_IS_ALIVE_RESPONSE);
    }
    
    /**
     * Format: HEADER(N bytes)TYPE(1 byte)NAME|IP|PING_TO_SERVER
     * @param encryptor  Encryptor of sending thread
     * @param user user, about whom is this info
     * @return data of UDPLib CLIENT_IS_ALIVE_RESPONSE datagram
     */
    public static byte[] createUserInfoDatagram(Encryptor encryptor, GroupUser user){
        String message = user.name+"|"+user.ip+"|"+user.getPingToHost();

        return createDatagram(encryptor, message, DatagramTypes.SERVER_CLIENTS_INFO);
    }
    
    /**
     * Format: HEADER(N bytes)TYPE(1 byte)NAME|IP
     * @param encryptor  Encryptor of sending thread
     * @param user user, about whom is this info
     * @return data of UDPLib SERVER_CLIENT_DEAD datagram
     */
    public static byte[] createUserDeadDatagram(Encryptor encryptor, GroupUser user){
        String message = user.name+"|"+user.ip+"|";

        return createDatagram(encryptor, message, DatagramTypes.SERVER_CLIENT_DEAD);
    }

    /**
     * @return (not encrypted) client request for server to send info about themselves
     */
    public static byte[] createExploreRequest(){
        byte[] toReturn =  new byte[DATAGRAM_HEADER.length+1];
        
        System.arraycopy(DATAGRAM_HEADER, 0, toReturn, 0, DATAGRAM_HEADER.length);
        toReturn[toReturn.length-1] = DatagramTypes.CLIENT_EXPLORE_REQUEST.index;
        
        return toReturn;
    }
    
    /**
     * Format: HEADER(N bytes)TYPE(1 byte)PASSWORD(1 byte - 1 - requires password/0)
     * @param requiresPassword true, if server requires password
     * @return (not encrypted) Info about server for exploring client
     */
    public static byte[] createExploreResponse(boolean requiresPassword){
        byte[] toReturn =  new byte[DATAGRAM_HEADER.length+2];
        
        System.arraycopy(DATAGRAM_HEADER, 0, toReturn, 0, DATAGRAM_HEADER.length);
        toReturn[toReturn.length-2] = DatagramTypes.SERVER_EXPLORE_RESPONSE.index;
        toReturn[toReturn.length-1] = (byte)(requiresPassword?1:0);
        
        return toReturn;
    }
    
    /**
     * @param data Data of datagram
     * @return DatagramType of this datagram or TRASH
     */
    public static DatagramTypes getDatagramType(byte[] data) {
        if(data.length >= DATAGRAM_HEADER.length + 1){
            for(int i=0; i < DATAGRAM_HEADER.length; i++){
                if(data[i] != DATAGRAM_HEADER[i]){
                    return DatagramTypes.TRASH;
                }
            }
            return DatagramTypes.getTypeByIndex(data[DATAGRAM_HEADER.length]);
        }else{
            return DatagramTypes.TRASH;
        }
    }
}