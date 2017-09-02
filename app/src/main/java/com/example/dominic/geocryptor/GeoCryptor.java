package com.example.dominic.geocryptor;

import android.os.AsyncTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Dominic on 8/28/2017.
 */
class GeoCryptor{

    //Creates a SHAH-256 hash of the string given, used as a 256 byte key for encryption
    public byte[] hashKey(String locKey) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(locKey.getBytes());
        return hash;
    }

    //Uses "AES" encryption to encrypt the string bytes with a key
    public byte[] encryptText (String text, String locKey) throws Exception{

        Cipher cipher = Cipher.getInstance("AES");
        SecretKeySpec sks = new SecretKeySpec(hashKey(locKey), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, sks);

        byte[] data = text.getBytes("UTF-8");
        byte[] encrypted = cipher.doFinal(data);

        return encrypted;
    }

    //Uses "AES" encryption to decrypt the provided bytes
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

    public byte[] encryptBytes(byte[] bytes, String locKey) throws Exception{
        Cipher cipher = Cipher.getInstance("AES");
        SecretKeySpec sks = new SecretKeySpec(hashKey(locKey), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, sks);

        return cipher.doFinal(bytes);
    }

}
