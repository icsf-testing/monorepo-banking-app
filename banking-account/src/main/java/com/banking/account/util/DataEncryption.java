================================================================================
FIXED CODE FOR: banking-account/src/main/java/com/banking/account/util/DataEncryption.java
================================================================================
package com.banking.account.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataEncryption {
    
    private static final String ALGORITHM = "AES";
    private static final String ENV_KEY_NAME = "BANKING_ENCRYPTION_KEY";
    private static final Logger LOGGER = Logger.getLogger(DataEncryption.class.getName());
    
    private static final SecretKey SECRET_KEY;
    private static final byte[] SALT = new byte[16];
    
    static {
        try {
            String envKey = getEnvironmentKey();
            new SecureRandom().nextBytes(SALT);
            SECRET_KEY = deriveKey(envKey);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize encryption key", e);
            throw new RuntimeException("Failed to initialize encryption key", e);
        }
    }
    
    private static String getEnvironmentKey() {
        String envKey = System.getenv(ENV_KEY_NAME);
        if (envKey == null || envKey.isEmpty()) {
            LOGGER.severe("Environment variable " + ENV_KEY_NAME + " is not set or empty");
            throw new IllegalStateException("Encryption key environment variable is not set");
        }
        return envKey;
    }
    
    private static SecretKey deriveKey(String keyMaterial) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        PBEKeySpec spec = new PBEKeySpec(keyMaterial.toCharArray(), SALT, 100000, 256);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), ALGORITHM);
    }
    
    public static String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, SECRET_KEY);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Encryption failed", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }
    
    public static String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }
        
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, SECRET_KEY);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Decryption failed", e);
            throw new RuntimeException("Decryption failed", e);
        }
    }

    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        return MessageDigest.isEqual(a, b);
    }
}