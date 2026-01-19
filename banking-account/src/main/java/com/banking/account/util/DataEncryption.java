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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.UUID;
import java.util.logging.Logger;

public class DataEncryption {
    
    private static final String ALGORITHM = "AES";
    private static final String ENV_VAR_NAME = "BANKING_ENCRYPTION_KEY";
    private static final Logger LOGGER = Logger.getLogger(DataEncryption.class.getName());
    
    private static final KeyRotationManager KEY_MANAGER;
    
    static {
        try {
            String envKey = System.getenv(ENV_VAR_NAME);
            if (envKey == null || envKey.isEmpty()) {
                throw new IllegalStateException("Encryption key environment variable " + ENV_VAR_NAME + " is not set");
            }
            KEY_MANAGER = new KeyRotationManager(envKey);
        } catch (Exception e) {
            throw new ExceptionInInitializerError("Failed to initialize encryption key: " + e.getMessage());
        }
    }
    
    private static class KeyRotationManager {
        private final ConcurrentHashMap<String, SecretKey> keys = new ConcurrentHashMap<>();
        private String currentKeyId;
        private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        
        public KeyRotationManager(String initialKey) throws NoSuchAlgorithmException {
            rotateKey(initialKey);
            scheduleKeyRotation();
        }
        
        private void scheduleKeyRotation() {
            scheduler.scheduleAtFixedRate(this::rotateKey, 30, 30, TimeUnit.DAYS);
        }
        
        private void rotateKey() {
            try {
                String newKey = generateNewKey();
                rotateKey(newKey);
            } catch (Exception e) {
                LOGGER.severe("Failed to rotate key: " + e.getMessage());
            }
        }
        
        private void rotateKey(String keyString) throws NoSuchAlgorithmException {
            SecretKey newKey = deriveKeyFromString(keyString);
            String keyId = UUID.randomUUID().toString();
            keys.put(keyId, newKey);
            currentKeyId = keyId;
            LOGGER.info("Key rotated. New key ID: " + keyId);
        }
        
        public SecretKey getCurrentKey() {
            return keys.get(currentKeyId);
        }
        
        public SecretKey getKey(String keyId) {
            return keys.get(keyId);
        }
        
        private String generateNewKey() {
            return UUID.randomUUID().toString();
        }
    }
    
    private static SecretKey deriveKeyFromString(String keyString) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = digest.digest(keyString.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(keyBytes, 0, 16, ALGORITHM);
    }
    
    public static String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, KEY_MANAGER.getCurrentKey());
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            String keyId = KEY_MANAGER.currentKeyId;
            byte[] combined = new byte[encryptedBytes.length + keyId.getBytes().length + 1];
            System.arraycopy(keyId.getBytes(), 0, combined, 0, keyId.getBytes().length);
            combined[keyId.getBytes().length] = ':';
            System.arraycopy(encryptedBytes, 0, combined, keyId.getBytes().length + 1, encryptedBytes.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            LOGGER.severe("Encryption failed: " + e.getMessage());
            throw new RuntimeException("Encryption failed", e);
        }
    }
    
    public static String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }
        
        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedText);
            int separatorIndex = -1;
            for (int i = 0; i < decoded.length; i++) {
                if (decoded[i] == ':') {
                    separatorIndex = i;
                    break;
                }
            }
            if (separatorIndex == -1) {
                throw new IllegalArgumentException("Invalid encrypted data format");
            }
            String keyId = new String(decoded, 0, separatorIndex, StandardCharsets.UTF_8);
            byte[] encryptedBytes = new byte[decoded.length - separatorIndex - 1];
            System.arraycopy(decoded, separatorIndex + 1, encryptedBytes, 0, encryptedBytes.length);
            
            SecretKey key = KEY_MANAGER.getKey(keyId);
            if (key == null) {
                throw new IllegalStateException("Key not found for ID: " + keyId);
            }
            
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOGGER.severe("Decryption failed: " + e.getMessage());
            throw new RuntimeException("Decryption failed", e);
        }
    }
}