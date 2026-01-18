package com.banking.account.domain;

import com.banking.account.util.DataEncryption;
import com.banking.core.domain.AccountType;
import com.banking.core.domain.Money;

import java.math.BigDecimal;

/**
 * Wrapper class that stores encrypted sensitive account data in memory.
 * This mitigates the "Insecure Data Storage" vulnerability by encrypting
 * sensitive fields before storage.
 * 
 * SECURITY: All sensitive fields (customerId, accountId, balance amount, currency) 
 * are encrypted at rest in memory using AES encryption. This provides protection 
 * against memory dump attacks and unauthorized access to financial data.
 * 
 * ⚠️ NOTE: This is a PoC implementation. For production:
 * - Use persistent database with encryption at rest
 * - Implement external key management (HSM, AWS KMS, HashiCorp Vault)
 * - Use hardware security modules for sensitive operations
 * - Follow PCI DSS, SOX, and banking security regulations
 */
public class EncryptedAccount {
    private final String encryptedAccountId;
    private final String encryptedCustomerId;
    private final AccountType accountType;
    private String encryptedBalanceAmount;  // Encrypted balance amount (mutable for updates)
    private String encryptedCurrency;       // Encrypted currency (mutable for updates)
    private boolean active;
    
    public EncryptedAccount(Account account) {
        this.encryptedAccountId = DataEncryption.encrypt(account.getAccountId());
        this.encryptedCustomerId = DataEncryption.encrypt(account.getCustomerId());
        this.accountType = account.getAccountType();
        Money balance = account.getBalance();
        // Encrypt balance amount and currency
        this.encryptedBalanceAmount = DataEncryption.encrypt(balance.getAmount().toString());
        this.encryptedCurrency = DataEncryption.encrypt(balance.getCurrency());
        this.active = account.isActive();
    }
    
    /**
     * Converts encrypted account back to Account domain object.
     * Decrypts all sensitive fields including balance information.
     */
    public Account toAccount() {
        // Decrypt balance information
        String decryptedAmount = DataEncryption.decrypt(encryptedBalanceAmount);
        String decryptedCurrency = DataEncryption.decrypt(encryptedCurrency);
        Money decryptedBalance = new Money(new BigDecimal(decryptedAmount), decryptedCurrency);
        
        return new Account(
            DataEncryption.decrypt(encryptedAccountId),
            DataEncryption.decrypt(encryptedCustomerId),
            accountType,
            decryptedBalance,
            active
        );
    }
    
    /**
     * Updates balance (used for deposit/withdraw operations).
     * Encrypts the new balance amount and currency before storing.
     */
    public void updateBalance(Money newBalance) {
        // Encrypt the new balance amount and currency before storing
        this.encryptedBalanceAmount = DataEncryption.encrypt(newBalance.getAmount().toString());
        this.encryptedCurrency = DataEncryption.encrypt(newBalance.getCurrency());
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
        return DataEncryption.decrypt(encryptedAccountId);
    }
    
    /**
     * Gets customer ID - decrypts on access.
     */
    public String getCustomerId() {
        return DataEncryption.decrypt(encryptedCustomerId);
    }
}

