package com.gmail.lepeska.martin.udplib.datagrams.explore;

import com.gmail.lepeska.martin.udplib.DatagramTypes;
import com.gmail.lepeska.martin.udplib.datagrams.ADatagram;
import static com.gmail.lepeska.martin.udplib.datagrams.ADatagram.bytesToString;
import com.gmail.lepeska.martin.udplib.util.Encryptor;
import java.net.DatagramPacket;

/**
 * Info about server for exploring client (not encrypted).
 * 
 * Format: HEADER(N bytes)TYPE(1B)PASSWORD("1" - requires password/"0")
 */
public class ExploreResponse extends ADatagram{
    
    private final String message;
    
    /**
     * @param requiresPassword true, if server requires password
     */
    public ExploreResponse(boolean requiresPassword) {
        super(Encryptor.DUMMY, DatagramTypes.SERVER_EXPLORE_RESPONSE);
        this.message = requiresPassword?"1":"0";
        this.data=createDatagramDataFromString(message);
    }
    
    /**
     * Reconstructs SERVER_CLIENTS_INFO from received packet.
     * 
     * @param source received packet containing info about true length of datagram
     */
    public ExploreResponse( DatagramPacket source) {
        super(Encryptor.DUMMY, DatagramTypes.SERVER_EXPLORE_RESPONSE);
        this.data=source.getData();
        this.message=bytesToString(unpackAndDecrypt(source));
    }

    @Override
    public String[] getStringMessage() {
        return message.split(ADatagram.DELIMITER);
    }

}
