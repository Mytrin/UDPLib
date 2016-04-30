package com.gmail.lepeska.martin.udplib;

import com.gmail.lepeska.martin.udplib.util.Encryptor;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Martin Lepeška
 */
public class EncryptorTest {

    @Test
    public void testSuccess() {
        Encryptor enc = new Encryptor("ABCDEFGHIJKLMNOP");
        String message = "message";
        byte[] encrypted = enc.encrypt(message.getBytes());
        
        assertEquals(new String(enc.decrypt(encrypted)), message);
    }
    
    @Test(expected=UDPLibException.class)
    public void testFail() {
        Encryptor enc = new Encryptor("ABCDEFGHIJKLMNOP");
        
        Encryptor enc2 = new Encryptor("0123456789111213");
        
        String message = "message";
        byte[] encrypted = enc.encrypt(message.getBytes());

        assertNotEquals(new String(enc2.decrypt(encrypted)), message);
    }
    
    @Test
    public void testDelay() {
        Encryptor enc = new Encryptor("ABCDEFGHIJKLMNOP");
        String message = "Average length message, Mytrin was here!";
        byte[] encrypted = enc.encrypt(message.getBytes());
        long decryptStart= System.currentTimeMillis();
        assertEquals(new String(enc.decrypt(encrypted)), message);
        System.out.println("DECRYPT TIME: "+(System.currentTimeMillis()-decryptStart));
    }
    
}