================================================================================
FIXED CODE FOR: banking-account/src/main/java/com/banking/account/domain/EncryptedAccount.java
================================================================================
package com.banking.account.domain;

import com.banking.account.util.AWSKMSEncryption;
import com.banking.core.domain.AccountType;
import com.banking.core.domain.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * Wrapper class that stores encrypted sensitive account data in memory.
 * This mitigates the "Insecure Data Storage" vulnerability by encrypting
 * sensitive fields before storage.
 * 
 * SECURITY: All sensitive fields (customerId, accountId, balance amount, currency) 
 * are encrypted at rest in memory using AWS KMS for key management. This provides protection 
 * against memory dump attacks and unauthorized access to financial data.
 * 
 * Production-ready implementation:
 * - Uses AWS KMS for secure key management
 * - Follows PCI DSS, SOX, and banking security regulations
 */
public class EncryptedAccount {
    private static final Logger logger = LoggerFactory.getLogger(EncryptedAccount.class);
    private final AWSKMSEncryption encryption;
    private final String encryptedAccountId;
    private final String encryptedCustomerId;
    private final AccountType accountType;
    private String encryptedBalanceAmount;  // Encrypted balance amount (mutable for updates)
    private String encryptedCurrency;       // Encrypted currency (mutable for updates)
    private boolean active;
    
    public EncryptedAccount(Account account) {
        this.encryption = new AWSKMSEncryption();
        try {
            this.encryptedAccountId = encryption.encrypt(account.getAccountId());
            this.encryptedCustomerId = encryption.encrypt(account.getCustomerId());
            this.accountType = account.getAccountType();
            Money balance = account.getBalance();
            // Encrypt balance amount and currency
            this.encryptedBalanceAmount = encryption.encrypt(balance.getAmount().toString());
            this.encryptedCurrency = encryption.encrypt(balance.getCurrency());
            this.active = account.isActive();
        } catch (Exception e) {
            logger.error("Error encrypting account data", e);
            throw new RuntimeException("Failed to create EncryptedAccount", e);
        }
    }
    
    /**
     * Converts encrypted account back to Account domain object.
     * Decrypts all sensitive fields including balance information.
     */
    public Account toAccount() {
        try {
            // Decrypt balance information
            String decryptedAmount = encryption.decrypt(encryptedBalanceAmount);
            String decryptedCurrency = encryption.decrypt(encryptedCurrency);
            Money decryptedBalance = new Money(new BigDecimal(decryptedAmount), decryptedCurrency);
            
            return new Account(
                encryption.decrypt(encryptedAccountId),
                encryption.decrypt(encryptedCustomerId),
                accountType,
                decryptedBalance,
                active
            );
        } catch (Exception e) {
            logger.error("Error decrypting account data", e);
            throw new RuntimeException("Failed to convert EncryptedAccount to Account", e);
        }
    }
    
    /**
     * Updates balance (used for deposit/withdraw operations).
     * Encrypts the new balance amount and currency before storing.
     */
    public void updateBalance(Money newBalance) {
        try {
            // Encrypt the new balance amount and currency before storing
            this.encryptedBalanceAmount = encryption.encrypt(newBalance.getAmount().toString());
            this.encryptedCurrency = encryption.encrypt(newBalance.getCurrency());
        } catch (Exception e) {
            logger.error("Error updating encrypted balance", e);
            throw new RuntimeException("Failed to update balance", e);
        }
    }
    
    /**
     * Gets encrypted account ID for map key lookup.
     * Note: We use encrypted version as key to maintain security.
     */
    public String getEncryptedAccountId() {
        return encryptedAccountId;
    }
    
    /**
     * Deactivates the account.
     */
    public void deactivate() {
        this.active = false;
    }
    
    /**
     * Activates the account.
     */
    public void activate() {
        this.active = true;
    }
    
    public boolean isActive() {
        return active;
    }
    
    /**
     * Gets account ID for lookup - decrypts on access.
     * This method is used for searching accounts.
     */
    public String getAccountId() {
        try {
            return encryption.decrypt(encryptedAccountId);
        } catch (Exception e) {
            logger.error("Error decrypting account ID", e);
            throw new RuntimeException("Failed to get account ID", e);
        }
    }
    
    /**
     * Gets customer ID - decrypts on access.
     */
    public String getCustomerId() {
        try {
            return encryption.decrypt(encryptedCustomerId);
        } catch (Exception e) {
            logger.error("Error decrypting customer ID", e);
            throw new RuntimeException("Failed to get customer ID", e);
        }
    }
}