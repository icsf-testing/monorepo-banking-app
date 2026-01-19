================================================================================
FIXED CODE FOR: banking-api/src/main/java/com/banking/api/dto/TransactionRequest.java
================================================================================
package com.banking.api.dto;

import javax.validation.constraints.*;
import org.hibernate.validator.constraints.Length;
import java.util.regex.Pattern;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.security.Key;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionRequest {
    private static final Logger logger = LoggerFactory.getLogger(TransactionRequest.class);
    private static final String ENCRYPTION_KEY = "ThisIsASecretKey";

    @NotBlank(message = "Account ID is required")
    @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "Invalid account ID format")
    private String accountId;

    @NotBlank(message = "From Account ID is required")
    @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "Invalid from account ID format")
    private String fromAccountId;

    @NotBlank(message = "To Account ID is required")
    @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "Invalid to account ID format")
    private String toAccountId;

    @Positive(message = "Amount must be positive")
    private double amount;

    @NotBlank(message = "Currency is required")
    @Length(min = 3, max = 3, message = "Currency must be a 3-letter code")
    private String currency;

    @NotBlank(message = "Description is required")
    @Length(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @PreAuthorize("hasRole('ROLE_ADMIN') or @securityService.hasAccountAccess(authentication, #accountId)")
    public String getAccountId() {
        try {
            return decrypt(accountId);
        } catch (Exception e) {
            logger.error("Error decrypting account ID", e);
            return null;
        }
    }

    public void setAccountId(String accountId) {
        try {
            this.accountId = encrypt(sanitizeInput(accountId));
        } catch (Exception e) {
            logger.error("Error encrypting account ID", e);
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or @securityService.hasAccountAccess(authentication, #fromAccountId)")
    public String getFromAccountId() {
        try {
            return decrypt(fromAccountId);
        } catch (Exception e) {
            logger.error("Error decrypting from account ID", e);
            return null;
        }
    }

    public void setFromAccountId(String fromAccountId) {
        try {
            this.fromAccountId = encrypt(sanitizeInput(fromAccountId));
        } catch (Exception e) {
            logger.error("Error encrypting from account ID", e);
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or @securityService.hasAccountAccess(authentication, #toAccountId)")
    public String getToAccountId() {
        try {
            return decrypt(toAccountId);
        } catch (Exception e) {
            logger.error("Error decrypting to account ID", e);
            return null;
        }
    }

    public void setToAccountId(String toAccountId) {
        try {
            this.toAccountId = encrypt(sanitizeInput(toAccountId));
        } catch (Exception e) {
            logger.error("Error encrypting to account ID", e);
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or @securityService.hasAccountAccess(authentication, #accountId)")
    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = sanitizeInput(currency);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = sanitizeInput(description);
    }

    private String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        return Pattern.compile("[<>&'\"]").matcher(input).replaceAll("");
    }

    private String encrypt(String value) throws Exception {
        Key key = new SecretKeySpec(ENCRYPTION_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(value.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    private String decrypt(String encrypted) throws Exception {
        Key key = new SecretKeySpec(ENCRYPTION_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encrypted));
        return new String(decrypted);
    }
}