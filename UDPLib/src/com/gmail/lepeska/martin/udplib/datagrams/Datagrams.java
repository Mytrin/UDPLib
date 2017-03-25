package com.gmail.lepeska.martin.udplib.datagrams;

import com.gmail.lepeska.martin.udplib.DatagramTypes;
import com.gmail.lepeska.martin.udplib.datagrams.explore.ExploreRequest;
import com.gmail.lepeska.martin.udplib.datagrams.explore.ExploreResponse;
import com.gmail.lepeska.martin.udplib.datagrams.files.FileShareBinaryPart;
import com.gmail.lepeska.martin.udplib.datagrams.files.FileShareFinish;
import com.gmail.lepeska.martin.udplib.datagrams.files.FileSharePartRequest;
import com.gmail.lepeska.martin.udplib.datagrams.files.FileShareTextPart;
import com.gmail.lepeska.martin.udplib.util.Encryptor;

import java.net.DatagramPacket;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Collection of useful utility functions for converting 
 * received bytes back into datagrams.
 */
public class Datagrams {
    
    private Datagrams(){}; //singleton
    
    /**
     * @param datagram Received bytes
     * @return Type of received datagram or TRASH
     */
    public static final DatagramTypes getDatagramType(DatagramPacket datagram){
        byte[] data = datagram.getData();
        if(datagram.getLength() > ADatagram.DATAGRAM_HEADER.length){
            return DatagramTypes.getTypeByIndex(data[ADatagram.DATAGRAM_HEADER.length]);
        }
        return DatagramTypes.TRASH;
    }
    
    /**
     * @param encryptor Group used encryptor
     * @param datagram Received bytes
     * @return ADatagram class representing received datagram or null for TRASH
     */
    public static final ADatagram reconstructDatagram(Encryptor encryptor, DatagramPacket datagram){
        DatagramTypes type = getDatagramType(datagram);
        
        switch(type){
            case TRASH: return null;
            //Group management
            case SERVER_IS_ALIVE_REQUEST: return new IsAliveDatagram(encryptor, datagram);
            case CLIENT_IS_ALIVE_RESPONSE: return new IsAliveDatagram(encryptor, datagram);
            case SERVER_CLIENTS_INFO: return new UserInfoDatagram(encryptor, datagram);
            case SERVER_CLIENT_DEAD: return new UserDeadDatagram(encryptor, datagram);
            //Messages
            case SERVER_MULTICAST_MESSAGE:
            case SERVER_UNICAST_MESSAGE:
            case CLIENT_MULTICAST_MESSAGE:
            case CLIENT_UNICAST_MESSAGE: return new MessageDatagram(encryptor, datagram);
            //File sharing
            case TEXT_FILE_SHARE_PART: return new FileShareTextPart(encryptor, datagram);
            case BINARY_FILE_SHARE_PART: return new FileShareBinaryPart(encryptor, datagram);
            case FILE_SHARE_FINISH: return new FileShareFinish(encryptor, datagram);
            case FILE_SHARE_PART_REQUEST: return new FileSharePartRequest(encryptor, datagram);
            //Access
            case CLIENT_ACCESS_REQUEST: return new AccessRequestDatagram(encryptor, datagram);
            case SERVER_ACCEPT_CLIENT_RESPONSE: return new AccessResponseDatagram(encryptor, datagram);
            //Exploring
            case CLIENT_EXPLORE_REQUEST: return new ExploreRequest(datagram);
            case SERVER_EXPLORE_RESPONSE: return new ExploreResponse(datagram);
        }
        
        Logger.getLogger(Datagrams.class.getName()).log(Level.SEVERE, "Failed to create datagram class for {0}", type);
        
        return null;
    }
    
    /**
     * FILENAME#INDEX#TOTAL#CHECKSUM#PART#0000 returns PART
     * 
     * @param parts Decrypted message with UDPLib data
     * @param index index of message part
     * @return Message with DELIMITERs
     */
    public static final String reconstructMessage(String[] parts, int index){
        String message = "";

        if(parts.length > 0 && index < parts.length){
            message = parts[index].trim();
            if(message.endsWith(ADatagram.DELIMITER)){
                return message.substring(0, message.length()-1);
            }else{
                return message;
            }
        }else{
            Logger.getLogger(Datagrams.class.getName())
                    .log(Level.SEVERE, "Requested to reconstructMessage on {0} "
                            + "with index {1}", new Object[]{Arrays.toString(parts), index});
            return message;
        }
    }
    
}
