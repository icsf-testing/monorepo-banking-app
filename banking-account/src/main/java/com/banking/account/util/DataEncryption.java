================================================================================
FIXED CODE FOR: banking-account/src/main/java/com/banking/account/util/DataEncryption.java
================================================================================
package com.banking.account.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Simple encryption utility for encrypting sensitive data in memory.
 * 
 * ⚠️ NOTE: This implementation uses AES encryption with a key derived from an environment variable.
 * For production, consider using:
 * - Hardware Security Modules (HSM) for key management
 * - Key rotation policies
 * - Industry-standard encryption libraries
 */
public class DataEncryption {
    
    private static final String ALGORITHM = "AES";
    private static final String ENV_VAR_NAME = "BANKING_ENCRYPTION_KEY";
    
    private static final SecretKey SECRET_KEY;
    
    static {
        try {
            String envKey = System.getenv(ENV_VAR_NAME);
            if (envKey == null || envKey.isEmpty()) {
                throw new IllegalStateException("Encryption key environment variable " + ENV_VAR_NAME + " is not set");
            }
            SECRET_KEY = deriveKeyFromString(envKey);
        } catch (Exception e) {
            throw new ExceptionInInitializerError("Failed to initialize encryption key: " + e.getMessage());
        }
    }
    
    private static SecretKey deriveKeyFromString(String keyString) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = digest.digest(keyString.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(keyBytes, 0, 16, ALGORITHM);
    }
    
    /**
     * Encrypts sensitive string data.
     * @param plainText The plain text to encrypt
     * @return Base64 encoded encrypted string
     */
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
            throw new RuntimeException("Encryption failed", e);
        }
    }
    
    /**
     * Decrypts encrypted string data.
     * @param encryptedText The Base64 encoded encrypted string
     * @return Decrypted plain text
     */
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
            throw new RuntimeException("Decryption failed", e);
        }
    }
}