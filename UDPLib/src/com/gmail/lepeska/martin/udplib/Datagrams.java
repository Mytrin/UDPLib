package com.gmail.lepeska.martin.udplib;

import com.gmail.lepeska.martin.udplib.util.Encryptor;
import com.gmail.lepeska.martin.udplib.client.GroupUser;
import com.gmail.lepeska.martin.udplib.server.GroupServerThread;
import java.net.DatagramPacket;
import java.net.InetAddress;


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
    /**Start sequence of bytes, which identifies, that datagram is sent by UDPLib*/
    public static final String DATAGRAM_HEADER_STRING = "UDPL";
    /**Start sequence of bytes, which identifies, that datagram is sent by UDPLib*/
    public static byte[] DATAGRAM_HEADER;
    /**Maximal safe datagram length = header#length#CONTENT*/
    public static final int MAXIMUM_DATA_LENGTH = 256;
    /**Special character, which marks certain parts of message*/
    public static final String DELIMITER = "#";
    
    public static final String ENCODING = "UTF-8";

    static{
        DATAGRAM_HEADER = stringToBytes(DATAGRAM_HEADER_STRING);
    }
    
    private Datagrams() {}
    
    /**
     * Creates UDPLib message datagram with given type. Private because of safety(messageType shouldn't be exposed)
     * 
     * HEAD(N B)TYPE(1 B)#MESSAGE
     * 
     * @param encryptor Encryptor of sending thread
     * @param message data to be sent
     * @param messageType identifies type of packet and role of sender
     * @return data of UDPLib message datagram
     */
    public static byte[] createDatagram(Encryptor encryptor, String message, DatagramTypes messageType){
        try{
            //I know it's ugly, just haven't find better way so far...
            String dataToEncrypt = messageType.index+DELIMITER+message;
            
            while(dataToEncrypt.getBytes(ENCODING).length%16 != 0){
                dataToEncrypt += '\0';
            }
            byte[] encodedMessage = encryptor.encrypt(dataToEncrypt.getBytes(ENCODING));

            byte[] finalBytes = new byte[DATAGRAM_HEADER.length+encodedMessage.length];
            
            System.arraycopy(DATAGRAM_HEADER, 0, finalBytes, 0, DATAGRAM_HEADER.length);
            System.arraycopy(encodedMessage, 0, finalBytes, DATAGRAM_HEADER.length, encodedMessage.length);
        
            return finalBytes;
        }catch(Exception e){
            throw new UDPLibException("Obtaining bytes form message failed: ", e);
        }
            
    }
    
    /**
     * Creates UDPLib message datagram data.
     * @param encryptor Encryptor of sending thread
     * @param message data to be sent
     * @param thread identifies role of sender
     * @param isMulticast true if message is to be sent to all group users
     * @return data of UDPLib message datagram
     */
    public static byte[] createMessageDatagram(Encryptor encryptor, String message, boolean isMulticast, AGroupThread thread){
        DatagramTypes type;
        
        if(thread instanceof GroupServerThread){
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
     * Format: HEADER(N bytes)TYPE#NAME|IP|PING_TO_SERVER
     * @param encryptor  Encryptor of sending thread
     * @param user user, about whom is this info
     * @return data of UDPLib CLIENT_IS_ALIVE_RESPONSE datagram
     */
    public static byte[] createUserInfoDatagram(Encryptor encryptor, GroupUser user){
        String message = user.name+DELIMITER+user.ip.getHostAddress()+DELIMITER+user.getPingToHost();

        return createDatagram(encryptor, message, DatagramTypes.SERVER_CLIENTS_INFO);
    }
    
    /**
     * Format: HEADER(N bytes)TYPE#NAME|IP
     * @param encryptor  Encryptor of sending thread
     * @param user user, about whom is this info
     * @return data of UDPLib SERVER_CLIENT_DEAD datagram
     */
    public static byte[] createUserDeadDatagram(Encryptor encryptor, GroupUser user){
        String message = user.name+DELIMITER+user.ip.getHostAddress();

        return createDatagram(encryptor, message, DatagramTypes.SERVER_CLIENT_DEAD);
    }

    /**
     * @return (not encrypted) client request for server to send info about themselves
     */
    public static byte[] createExploreRequest(){
        return stringToBytes(DATAGRAM_HEADER_STRING+DatagramTypes.CLIENT_EXPLORE_REQUEST.index);
    }
    
    /**
     * Format: HEADER(N bytes)TYPE#PASSWORD(1 byte - 1 - requires password/0)
     * @param requiresPassword true, if server requires password
     * @return (not encrypted) Info about server for exploring client
     */
    public static byte[] createExploreResponse(boolean requiresPassword){
        return stringToBytes(DATAGRAM_HEADER_STRING+DatagramTypes.SERVER_EXPLORE_RESPONSE.index+DELIMITER+(requiresPassword?"1":"0"));
    }
    
    /**
     * 
     * @param encryptor Encryptor of sending thread
     * @param username Client's name in group
     * @param password Group password
     * @return data of UDPLib CLIENT_ACCESS_REQUEST datagram
     */
    public static byte[] createClientAccessRequest(Encryptor encryptor, String username, String password){
        String message = password+DELIMITER+username;
        
        return createDatagram(encryptor, message, DatagramTypes.CLIENT_ACCESS_REQUEST);
    }
    
    /**
     * HEAD(N B)TYPE#GROUP|USE_PASSWORD
     * @param encryptor Encryptor of sending thread
     * @param groupAddress Group address of sending thread
     * @param clientAddress IP of client to let him know which interface he should use
     * @param accept true if recipient is accepted to group
     * @return data of UDPLib SERVER_ACCEPT_CLIENT_RESPONSE datagram
     */
    public static byte[] createServerClientAccessResponse(Encryptor encryptor, InetAddress groupAddress, InetAddress clientAddress, boolean accept){
        String message = groupAddress.getHostAddress()+DELIMITER+(accept?1:0)+DELIMITER+clientAddress.getHostAddress();
        
        return createDatagram(encryptor, message, DatagramTypes.SERVER_ACCEPT_CLIENT_RESPONSE);
    }
    
    /**
     * HEAD(N B)TYPE#FILENAME#INDEX#TOTAL#PART#
     * @param encryptor Encryptor of sending thread
     * @param fileName - String, under which should be the SharedFile stored in Map
     * @param filePart - Line or part of line of file to send
     * @param index - index of this part in array of SharedFile
     * @param total - count of all parts, which were or will be sent, so client can initialize array of SharedFile
     * @return data of UDPLib SERVER_FILE_SHARE_PART datagram
     */
    public static byte[] createServerFileSharePart(Encryptor encryptor, String fileName, String filePart, int index, int total){
        String message = fileName+DELIMITER+index+DELIMITER+total+DELIMITER+filePart+DELIMITER;
        
        return createDatagram(encryptor, message, DatagramTypes.SERVER_FILE_SHARE_PART);
    }
    
    /**
     * HEAD(N B)TYPE#FILENAME#
     * @param encryptor Encryptor of sending thread
     * @param fileName - String, under which should be the SharedFile stored in Map
     * @return data of UDPLib SERVER_FILE_SHARE_FINISH datagram
     */
    public static byte[] createServerFileShareFinish(Encryptor encryptor, String fileName){
        String message = fileName+DELIMITER;
        
        return createDatagram(encryptor, message, DatagramTypes.SERVER_FILE_SHARE_FINISH);
    }
    
    /**
     * HEAD(N B)TYPE#FILENAME#INDEX
     * @param encryptor Encryptor of sending thread
     * @param fileName - String, under which should be the SharedFile stored in Map
     * @param index - index of filePart, which is client missing
     * @return data of UDPLib CLIENT_FILE_SHARE_PART_REQUEST datagram
     */
    public static byte[] createClientFileSharePartRequest(Encryptor encryptor, String fileName, int index){
        String message = fileName+DELIMITER+index;
        
        return createDatagram(encryptor, message, DatagramTypes.CLIENT_FILE_SHARE_PART_REQUEST);
    }
    
    /**
     * @param message Data of datagram returned from unpack()
     * @return DatagramType of this datagram or TRASH
     */
    public static DatagramTypes getDatagramType(String message) {
        int index = message.indexOf(DELIMITER);
        String type;
        if(index == -1){
            type = message;
        }else{
            type = ""+message.substring(0, index);
        }
        
        if(type.matches("\\d*")){
             return DatagramTypes.getTypeByIndex(Integer.parseInt(type));
        }else{
            return DatagramTypes.TRASH;
        }
    }
    
    /**
     * @param message recently received data, which might not be encrypted
     * @return DatagramType of this datagram or TRASH
     */
    public static boolean isExploreDatagram(String message) {
        return message.trim().equals(DATAGRAM_HEADER_STRING+DatagramTypes.CLIENT_EXPLORE_REQUEST.index);
    }
    
    /**
     * @param data received data
     * @param encryptor class responsible for decrypting
     * @param source packet with information about data length
     * @return decrypted datagram data without UDPLib header containing datagram type on 0 index
     */
    public static String unpack(byte[] data, Encryptor encryptor, DatagramPacket source){
        byte[] toDecrypt = new byte[source.getLength() - DATAGRAM_HEADER.length];
        System.arraycopy(data, DATAGRAM_HEADER.length, toDecrypt, 0, toDecrypt.length);
        
        return bytesToString(encryptor.decrypt(toDecrypt));
    }
    
    /**
     * Follows after unpack(), removes part informing about type of datagram
     * @param data previously unpacked data 
     * @return data without type of datagram
     */
    public static String unpack2(String data){
        return data.trim().substring(data.indexOf(DELIMITER)+1);
    }
    
    /**
     * @param string String to convert
     * @return byte[] containing string with UDPLib encoding
     */
    public static byte[] stringToBytes(String string){
        try{
            return string.getBytes(ENCODING);
        }catch(Exception e){
            return string.getBytes();
        }
    }
    
    /**
     * @param bytes byte[] containing string with UDPLib encoding
     * @return string with UDPLib encoding
     */
    public static String bytesToString(byte[] bytes){
        try{
            return new String(bytes, ENCODING);
        }catch(Exception e){
            return new String(bytes);
        }
    }
}
