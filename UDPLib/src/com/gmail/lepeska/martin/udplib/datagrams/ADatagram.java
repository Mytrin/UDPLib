package com.gmail.lepeska.martin.udplib.datagrams;

import com.gmail.lepeska.martin.udplib.DatagramTypes;
import com.gmail.lepeska.martin.udplib.UDPLibException;
import com.gmail.lepeska.martin.udplib.util.ConfigLoader;
import com.gmail.lepeska.martin.udplib.util.Encryptor;
import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * 
 * Every type of Datagram has its own class, however all are using either text
 *  or binary data representation.
 * 
 * DATAGRAM CONTENT(bytes): DATAGRAM_HEADER + DATAGRAM TYPE + MESSAGE/DATA
 * MESSAGE CONTENT(string): value1#value2#value3...
 * 
 * @author Martin Lepeška
 */
public abstract class ADatagram {
    
    /**Start sequence of bytes, which identifies, that datagram is sent by UDPLib*/
    public static final byte[] DATAGRAM_HEADER;
    /**Maximal safe datagram length = header#length#CONTENT*/
    public static final int MAXIMUM_DATAGRAM_LENGTH;
    /**Special character, which marks certain parts of message*/
    public static final String DELIMITER;
    /**Force encoding to prevent ?*/
    public static final String ENCODING;
    /* the problem with String header and byte[] data is how to recognize where which part ends
     * The solution I came up with is CLIENT/SERVER configured size for header*/
    protected static final int HEADER_SIZE;
    
    //TODO config
    static{
        DATAGRAM_HEADER = stringToBytes(ConfigLoader.getString("header-string", "UDPL"));
        MAXIMUM_DATAGRAM_LENGTH = ConfigLoader.getInt("max-datagram-length", 256);
        DELIMITER = ConfigLoader.getString("delimiter", "#");
        ENCODING = ConfigLoader.getString("encoding", "UTF-8");
        HEADER_SIZE = ConfigLoader.getInt("binary-file-header-size", 32);
    }

    /**Encryption/Decryption*/
    protected Encryptor encryptor;
    /**Datagram bytes*/
    protected byte[] data;
    /**Object purpose*/
    protected DatagramTypes type;

    /**
     * 
     * @param encryptor Encryption/Decryption
     * @param type Object purpose
     */
    protected ADatagram(Encryptor encryptor, DatagramTypes type) {
        this.encryptor = encryptor;
        this.type = type;
    }

    /**
     * @return unpacked String data divided by DELIMITER or null, if datagram is binary
     */
    public String[] getStringMessage(){
        return null;
    }
    
    /**
     * @return unpacked binary data or null, if datagram is text
     */
    public byte[] getBinaryMessage() {
        return null;
    }
    
    /**
     * Creates datagram without data.
     * 
     * @return data of UDPLib message datagram
     */
    protected final byte[] createEmptyDatagram(){
        try{
            byte[] finalBytes = new byte[DATAGRAM_HEADER.length+1];
            System.arraycopy(DATAGRAM_HEADER, 0, finalBytes, 0, DATAGRAM_HEADER.length);
            finalBytes[DATAGRAM_HEADER.length] = (byte)type.index;
        
            return finalBytes;
        }catch(Exception e){
            throw new UDPLibException("Obtaining bytes form message failed: ", e);
        }
    }
    
    /**
     * Creates encrypted datagram data with given message.
     * 
     * @param message data to be sent
     * @return data of UDPLib message datagram
     */
    protected final byte[] createDatagramDataFromString(String message){
        try{
            //I know this is ugly, just haven't find better way so far...
            String dataToEncrypt = message+DELIMITER;
            
            while(dataToEncrypt.getBytes(ENCODING).length%16 != 0){
                dataToEncrypt += '\0';
            }
            byte[] encodedMessage = encryptor.encrypt(stringToBytes(dataToEncrypt));

            byte[] finalBytes = new byte[DATAGRAM_HEADER.length+1+encodedMessage.length];
            System.arraycopy(DATAGRAM_HEADER, 0, finalBytes, 0, DATAGRAM_HEADER.length);
            finalBytes[DATAGRAM_HEADER.length] = (byte)type.index;
            System.arraycopy(encodedMessage, 0, finalBytes, DATAGRAM_HEADER.length+1, encodedMessage.length);
        
            return finalBytes;
        }catch(Exception e){
            throw new UDPLibException("Obtaining bytes form message failed: ", e);
        }
    }
    
     /**
     * @param info informations about data
     * @param data - binary data
     * @return data of UDPLib SERVER_FILE_SHARE_PART datagram
     */
    protected final byte[] createDatagramDataFromStringAndBytes(String info, byte[] data){
        String header = info+DELIMITER;

        byte[] headerData = stringToBytes(header);
        
        //the problem with String header a byte[] data is how to recognize where which part ends
        //The solution I came up with is CLIENT/SERVER configured size for header
        if(headerData.length > HEADER_SIZE) throw new UDPLibException("File datagram header size too long!");
        
        int dataLength = HEADER_SIZE + data.length;
        if(dataLength%16!=0){ //Encryptor takes only 16
            dataLength+=16-dataLength%16;
        }
        byte[] datagramData = new byte[dataLength];
        System.arraycopy(headerData, 0, datagramData, 0, headerData.length);
        System.arraycopy(data, 0, datagramData, HEADER_SIZE, data.length);
        
        byte[] encryptedData = encryptor.encrypt(datagramData);
        
        byte[] datagram = new byte[DATAGRAM_HEADER.length + 1 + encryptedData.length];
        System.arraycopy(DATAGRAM_HEADER, 0, datagram, 0, DATAGRAM_HEADER.length);
        datagram[DATAGRAM_HEADER.length] = (byte)DatagramTypes.SERVER_BINARY_FILE_SHARE_PART.index;
        System.arraycopy(encryptedData, 0, datagram, DATAGRAM_HEADER.length+1, encryptedData.length);
        
        return datagram;
    }
    
    /**
     * @param target who should receive this datagram
     * @param port target port
     * @return DatagramPacket for sending
     */
    public DatagramPacket createPacket(InetAddress target, int port){
        return new DatagramPacket(data, data.length, target, port); 
    }
    
    /**
     * @param data received datagram data
     * @param source packet with information about data length
     * @return datagram data without header
     */
    protected final byte[] unpackNotEncrypted(byte[] data, DatagramPacket source) {
       byte[] dataWithoutHeader = new byte[source.getLength()  - DATAGRAM_HEADER.length -1];
       System.arraycopy(data, DATAGRAM_HEADER.length+1, dataWithoutHeader, 0, dataWithoutHeader.length);
       
       return dataWithoutHeader;
    }
    
    /**
     * @param source packet with information about data length
     * @return decrypted datagram data without UDPLib header
     */
    protected final byte[] unpackAndDecrypt(DatagramPacket source){
        //Remove header
        byte[] toDecrypt = new byte[source.getLength() - DATAGRAM_HEADER.length -1]; //-1 for DATAGRAM_TYPE
        System.arraycopy(source.getData(), DATAGRAM_HEADER.length+1, toDecrypt, 0, toDecrypt.length);
        
        return encryptor.decrypt(toDecrypt);
    }
    
    
    /**
     * @return Object purpose
     */
    public DatagramTypes getType() {
        return type;
    }
    
    /**
     * @param data received datagram data
     * @return The type of UDPLib datagram or TRASH
     */
    public static DatagramTypes getDatagramType(byte[] data){
        if(data.length < DATAGRAM_HEADER.length+1) return DatagramTypes.TRASH;
        
        for(int i=0; i < DATAGRAM_HEADER.length; i++){
            if(data[i] != DATAGRAM_HEADER[i]) return DatagramTypes.TRASH;
        }
        
        return DatagramTypes.getTypeByIndex(data[DATAGRAM_HEADER.length]);
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
            return new String(bytes).trim();
        }
    }
}
