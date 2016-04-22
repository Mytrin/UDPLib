package com.gmail.lepeska.martin.udplib;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
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
        
        if(password.length() < 16){
            for(int i = 0; i < 16-password.length(); i++){
                password += ".";
            }
        }else if(password.length() > 16){
            password = password.substring(0, 15);
        }

        aesKey = new SecretKeySpec(password.getBytes(), "AES");
	
        try{
            cipher = Cipher.getInstance("AES");
        }catch(NoSuchAlgorithmException | NoSuchPaddingException e){
            throw new UDPLibException("Failed to create Encryptor: ", e);
        }
        
    }
    
    /**
     * 
     * @param decrypted bytes with message to encode
     * @return encrypt message
     */
    public byte[]encrypt(byte[] decrypted){
         if(aesKey != null){
             try{
                cipher.init(Cipher.ENCRYPT_MODE, aesKey);
                return cipher.doFinal(decrypted);
            }catch(IllegalBlockSizeException | InvalidKeyException  | BadPaddingException e ){
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
    public byte[]decrypt(byte[] encrypted){
        if(aesKey != null){
            try{
               cipher.init(Cipher.DECRYPT_MODE, aesKey);
               return cipher.doFinal(encrypted);
            }catch(IllegalBlockSizeException | InvalidKeyException | BadPaddingException e){
               throw new UDPLibException("Failed to decrypt message: ", e);
            }
        }
        return encrypted;
    }

}