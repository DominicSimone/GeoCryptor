package com.example.dominic.geocryptor;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Dominic on 8/28/2017.
 *
 * This is a utility class to encrypy/decrypt text with AES encryption
 *
 * Designed in this application to encrypt/decrypt with location data.
 *
 * Note: location key is made in the LocationFinder class
 */
class GeoCryptor{

    /**
     * Creates a SHAH-256 hash of the string given, used as a 256 byte key for encryption
     *
     * @param locKey the location key required to create a hash key
     * @return the hash key required to encrypt
     */
    public byte[] hashKey(String locKey) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(locKey.getBytes());
        return hash;
    }

    /**
     * Uses "AES" encryption to encrypt the string bytes with a key
     *
     * @param text to encrypt.
     * @param locKey the location key required to encrypt
     * @return encrypted bytes version of the text provided
     */
    public byte[] encryptText (String text, String locKey) throws Exception{

        Cipher cipher = Cipher.getInstance("AES");
        SecretKeySpec sks = new SecretKeySpec(hashKey(locKey), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, sks);

        byte[] data = text.getBytes("UTF-8");
        byte[] encrypted = cipher.doFinal(data);

        return encrypted;
    }

    //Uses "AES" encryption to decrypt the provided bytes

    /**
     * Uses "AES" encryption to decrypt the provided bytes.
     * @param encrypted the encrypted bytes to decrypt.
     * @param locKey the location key to decrypt the bytes with.
     * @return decrypted byte[] version of the data provided.
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     */
    public byte[] decryptBytes(byte[] encrypted, String locKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {

        byte[] decrypted = null;

        Cipher cipher = Cipher.getInstance("AES");
        SecretKeySpec sks = new SecretKeySpec(hashKey(locKey), "AES");
        cipher.init(Cipher.DECRYPT_MODE, sks);

        try {
            decrypted = cipher.doFinal(encrypted);
        }
        catch( Exception e){
            System.out.print(e.getMessage());
        }

        return decrypted;
    }

    /**
     * This method encrypts raw bytes given to it with a location key using "AES".
     * @param bytes the raw data to encrypt.
     * @param locKey the location key required to encrypt.
     * @return
     * @throws Exception
     */
    public byte[] encryptBytes(byte[] bytes, String locKey) throws Exception{
        Cipher cipher = Cipher.getInstance("AES");
        SecretKeySpec sks = new SecretKeySpec(hashKey(locKey), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, sks);

        return cipher.doFinal(bytes);
    }

}
