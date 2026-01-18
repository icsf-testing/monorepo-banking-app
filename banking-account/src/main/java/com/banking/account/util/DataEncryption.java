package com.banking.account.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Simple encryption utility for encrypting sensitive data in memory.
 * 
 * ⚠️ NOTE: This is a basic PoC implementation using AES encryption.
 * For production, use:
 * - Hardware Security Modules (HSM) for key management
 * - Key rotation policies
 * - Secure key storage (not in code)
 * - Industry-standard encryption libraries
 */
public class DataEncryption {
    
    private static final String ALGORITHM = "AES";
    
    // ⚠️ SECURITY: Hardcoded key for PoC only - NEVER use in production!
    // Production should use external key management (AWS KMS, HashiCorp Vault, etc.)
    private static final String SECRET_KEY_STRING = "BankingSecretKey!"; // 16 bytes for AES-128
    
    private static final SecretKey SECRET_KEY;
    
    static {
        try {
            // Convert string key to SecretKeySpec
            SECRET_KEY = new SecretKeySpec(SECRET_KEY_STRING.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize encryption key", e);
        }
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

