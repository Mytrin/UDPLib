package com.gmail.lepeska.martin.udplib;

/**
 *
 * UDPLib's own exception class, nothing special.
 * 
 * @author Martin Lepeška
 */
public class UDPLibException extends RuntimeException{

    /**
     * @param error Error message
     */
    public UDPLibException(String error) {
        super(error);
    }
    
    /**
     * @param error Fail reason
     * @param message Error message
     */
    public UDPLibException(String message, Throwable error) {
        super(message, error);
    }
}