package com.gmail.lepeska.martin.udplib.util;

import com.gmail.lepeska.martin.udplib.UDPLibException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * Simple class for encrypting datagram's data in the network. 
 * 
 * @author Martin Lepeška
 */
public class Encryptor {
    /**Class responsible for cryptography*/
    private Cipher cipher;
    /**Key containing password, needs to be 16 characters long(128bit)*/
    private final Key aesKey;
    
    /**
     * Dummy encryptor, which returns given bytes as encoded
     */
    public Encryptor() {
        this.aesKey = null;
    }
    
    /**
     * @param password password, needs to be 16 characters long(128bit)
     */
    public Encryptor(String password) {
        try{
            byte[] passwordBytes = password.getBytes("UTF-8");
            byte[] validatedPassword = new byte[16];
        
            if(passwordBytes.length > 16){
                System.arraycopy(passwordBytes, 0, validatedPassword, 0, 15);
            }else if(passwordBytes.length < 16){
                System.arraycopy(passwordBytes, 0, validatedPassword, 0, passwordBytes.length-1);
            }else{
                validatedPassword = passwordBytes;
            }
        
            aesKey = new SecretKeySpec(validatedPassword, "AES");
            
            cipher = Cipher.getInstance("AES/ECB/NoPadding");
        }catch(UnsupportedEncodingException | NoSuchAlgorithmException | NoSuchPaddingException e){
            throw new UDPLibException("Failed to create Encryptor: ", e);
        }
        
    }
    
    /**
     * 
     * @param decrypted bytes with message to encode
     * @return encrypt message
     */
    public synchronized byte[] encrypt(byte[] decrypted){ //Forgot use synchronized and it ocasionally returned trash
         if(aesKey != null){
             try{
                cipher.init(Cipher.ENCRYPT_MODE, aesKey);
                
                byte[] toReturn  = cipher.doFinal(decrypted);

                return toReturn;
            }catch(InvalidKeyException | IllegalBlockSizeException | BadPaddingException e ){
              throw new UDPLibException("Failed to decrypt message: ", e);
            } 
         }
         
         
         
         return decrypted;
    }
    
    /**
     * Throws UDPLibException, if fails.
     * @param encrypted bytes with message to decode
     * @return  decrypted message
     */
    public synchronized byte[] decrypt(byte[] encrypted){ //Forgot use synchronized and it ocasionally returned trash
        if(aesKey != null){
            try{
               cipher.init(Cipher.DECRYPT_MODE, aesKey);

               byte[] toReturn = cipher.doFinal(encrypted);

               return toReturn;
            }catch(InvalidKeyException | IllegalBlockSizeException | BadPaddingException e){
               throw new UDPLibException("Failed to decrypt message: ", e);
            }
        }
        return encrypted;
    }

}