package com.banking.account.domain;

import com.banking.account.util.DataEncryption;
import com.banking.core.domain.AccountType;
import com.banking.core.domain.Money;

/**
 * Wrapper class that stores encrypted sensitive account data in memory.
 * This mitigates the "Insecure Data Storage" vulnerability by encrypting
 * sensitive fields before storage.
 * 
 * SECURITY: Sensitive fields (customerId, accountId) are encrypted at rest in memory.
 * Note: Balance information is not encrypted here as it's wrapped in Money object.
 * For full production security, implement database encryption.
 */
public class EncryptedAccount {
    private final String encryptedAccountId;
    private final String encryptedCustomerId;
    private final AccountType accountType;
    private Money balance;
    private boolean active;
    
    public EncryptedAccount(Account account) {
        this.encryptedAccountId = DataEncryption.encrypt(account.getAccountId());
        this.encryptedCustomerId = DataEncryption.encrypt(account.getCustomerId());
        this.accountType = account.getAccountType();
        this.balance = account.getBalance();
        this.active = account.isActive();
    }
    
    /**
     * Converts encrypted account back to Account domain object.
     */
    public Account toAccount() {
        return new Account(
            DataEncryption.decrypt(encryptedAccountId),
            DataEncryption.decrypt(encryptedCustomerId),
            accountType,
            balance,
            active
        );
    }
    
    /**
     * Updates balance (used for deposit/withdraw operations).
     */
    public void updateBalance(Money newBalance) {
        this.balance = newBalance;
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

