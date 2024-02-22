package com.foobar.tools.lockbox;

import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtils {
    private static SecureRandom secRnd = null;
    
    public static byte [] ValueToKeybyte(byte [] value, byte [] salt, int count) throws Exception {
    	if (value == null) return null;    	
    	MessageDigest digest = MessageDigest.getInstance("SHA-256");
    	byte [] hv = null;
    	digest.update(salt);
    	byte input[] = value;
    	for(int x = 0; x < count; x++) {
    		hv = digest.digest(input);
    		input = hv;
    	}
    	return hv;
    }

    public static byte [] GenerateRandomKey(int bytes) {
        if (com.foobar.tools.lockbox.CryptoUtils.secRnd == null) {
            secRnd = new SecureRandom();
            byte buffer[] = new byte[32];
            // Call me superstitious but I do not want the first bunch of bytes the PRNG generates.
            secRnd.nextBytes(buffer);
        }
        byte key[] = new byte[bytes];
        secRnd.nextBytes(key);
        return key;
    }
    
    //
    // This is generating random bytes. It is not key data (probably IV or something) so does not need to be secure random
    // using a different random object for this one so we do not share random data with keys
    public static byte [] GenerateRandomData(int bytes) {
    	SecureRandom rnd = new SecureRandom();
    	byte data[] = new byte[bytes];
    	rnd.nextBytes(data);
    	return data;
    }
    public static byte [] Encrypt(byte [] data, byte[] iv, byte[] key) throws Exception {
    	if (data == null || iv == null || key == null) {
    		throw new IllegalArgumentException("Bad data passed into encrypt");
    	}
   		SecretKeySpec ss = new SecretKeySpec(key, "AES");
   		Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
   		aesCipher.init(Cipher.ENCRYPT_MODE, ss, new IvParameterSpec(iv));
   		byte [] output = aesCipher.doFinal(data);
   		return output;
    }

	public static byte[] SHA256(byte[] value) throws Exception {
		if (value == null) throw new IllegalArgumentException("Need to pass in data to hash");
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
    	return digest.digest(value);
	}

	public static byte[] Decrypt(byte[] data, byte[] iv, byte[] key) throws Exception {
    	if (data == null || iv == null || key == null) {
    		throw new IllegalArgumentException("Bad data passed into decrypt");
    	}
   		SecretKeySpec ss = new SecretKeySpec(key, "AES");
   		Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
   		aesCipher.init(Cipher.DECRYPT_MODE, ss, new IvParameterSpec(iv));
   		try {
   			byte [] output = aesCipher.doFinal(data);
   	   		return output;
   		} catch (BadPaddingException e) {
   			// This usually means we got the wrong key
   			// In this case we are just going to return null;
   			return null;
   		}
	}
}