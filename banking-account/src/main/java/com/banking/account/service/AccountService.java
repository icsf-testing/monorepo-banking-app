package com.banking.account.service;

import com.banking.account.domain.Account;
import com.banking.account.domain.EncryptedAccount;
import com.banking.account.util.DataEncryption;
import com.banking.core.domain.AccountType;
import com.banking.core.domain.Money;
import com.banking.core.exception.InvalidAccountException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing bank accounts.
 * 
 * SECURITY IMPROVEMENT: Insecure Data Storage Vulnerability Fixed
 * ===============================================================
 * This implementation addresses the "Insecure Data Storage" vulnerability by:
 * - Encrypting sensitive fields (customerId, accountId) before storing in memory
 * - Using AES encryption for data at rest in memory
 * - Decrypting data only when needed for business logic
 * 
 * ⚠️ NOTE: This is a PoC mitigation. Full production solution requires:
 * - Persistent database with encryption at rest
 * - Secure key management (HSM, AWS KMS, HashiCorp Vault)
 * - Key rotation policies
 * - Hardware Security Modules for sensitive operations
 * - Comprehensive audit logging
 * - Access controls and compliance (PCI DSS, SOX, GDPR)
 * 
 * Current implementation uses in-memory encrypted storage which:
 * - ✅ Encrypts sensitive data in memory (mitigates memory dump attacks)
 * - ⚠️ Data still lost on restart (use persistent DB for production)
 * - ⚠️ Uses hardcoded key (use external key management in production)
 */
public class AccountService {
    // SECURITY: Using encrypted account storage - sensitive fields encrypted in memory
    // Account IDs and Customer IDs are encrypted using AES encryption
    private final Map<String, EncryptedAccount> accounts = new ConcurrentHashMap<>();

    public Account createAccount(String customerId, AccountType accountType, Money initialBalance) {
        Account account = new Account(customerId, accountType, initialBalance);
        EncryptedAccount encryptedAccount = new EncryptedAccount(account);
        // Store using encrypted account ID as key
        accounts.put(encryptedAccount.getEncryptedAccountId(), encryptedAccount);
        return account;
    }

    public Account getAccount(String accountId) {
        // Find account by decrypting and comparing IDs
        EncryptedAccount encryptedAccount = findAccountById(accountId);
        if (encryptedAccount == null) {
            throw new InvalidAccountException("Account not found: " + accountId);
        }
        return encryptedAccount.toAccount();
    }
    
    private EncryptedAccount findAccountById(String accountId) {
        String encryptedAccountId = DataEncryption.encrypt(accountId);
        return accounts.get(encryptedAccountId);
    }
    
    private EncryptedAccount findAccountByIdOrThrow(String accountId) {
        EncryptedAccount encryptedAccount = findAccountById(accountId);
        if (encryptedAccount == null) {
            throw new InvalidAccountException("Account not found: " + accountId);
        }
        return encryptedAccount;
    }

    public List<Account> getAccountsByCustomer(String customerId) {
        List<Account> customerAccounts = new ArrayList<>();
        for (EncryptedAccount encryptedAccount : accounts.values()) {
            if (encryptedAccount.getCustomerId().equals(customerId)) {
                customerAccounts.add(encryptedAccount.toAccount());
            }
        }
        return customerAccounts;
    }

    public List<Account> getAllAccounts() {
        List<Account> allAccounts = new ArrayList<>();
        for (EncryptedAccount encryptedAccount : accounts.values()) {
            allAccounts.add(encryptedAccount.toAccount());
        }
        return allAccounts;
    }

    public void deactivateAccount(String accountId) {
        EncryptedAccount encryptedAccount = findAccountByIdOrThrow(accountId);
        encryptedAccount.deactivate();
    }

    public void activateAccount(String accountId) {
        EncryptedAccount encryptedAccount = findAccountByIdOrThrow(accountId);
        encryptedAccount.activate();
    }

    public Money getBalance(String accountId) {
        Account account = getAccount(accountId);
        return account.getBalance();
    }
    
    /**
     * Deposits money into an account.
     * This method updates the encrypted storage with the new balance.
     */
    public void deposit(String accountId, Money amount) {
        EncryptedAccount encryptedAccount = findAccountByIdOrThrow(accountId);
        Account account = encryptedAccount.toAccount();
        account.deposit(amount);
        encryptedAccount.updateBalance(account.getBalance());
    }
    
    /**
     * Withdraws money from an account.
     * This method updates the encrypted storage with the new balance.
     */
    public void withdraw(String accountId, Money amount) {
        EncryptedAccount encryptedAccount = findAccountByIdOrThrow(accountId);
        Account account = encryptedAccount.toAccount();
        account.withdraw(amount);
        encryptedAccount.updateBalance(account.getBalance());
    }
    
    /**
     * Updates account balance (used internally by transaction operations).
     * This method preserves encryption while updating balance.
     */
    void updateAccountBalance(String accountId, Money newBalance) {
        EncryptedAccount encryptedAccount = findAccountByIdOrThrow(accountId);
        encryptedAccount.updateBalance(newBalance);
    }
}

