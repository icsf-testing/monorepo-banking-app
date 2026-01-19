================================================================================
FIXED CODE FOR: banking-account/src/main/java/com/banking/account/service/AccountService.java
================================================================================
package com.banking.account.service;

import com.banking.account.domain.Account;
import com.banking.account.domain.EncryptedAccount;
import com.banking.core.domain.AccountType;
import com.banking.core.domain.Money;
import com.banking.core.exception.InvalidAccountException;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DecryptRequest;
import software.amazon.awssdk.services.kms.model.DecryptResponse;
import software.amazon.awssdk.services.kms.model.EncryptRequest;
import software.amazon.awssdk.services.kms.model.EncryptResponse;
import software.amazon.awssdk.core.SdkBytes;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AccountService {
    private final Map<String, EncryptedAccount> accounts = new ConcurrentHashMap<>();
    private final KmsClient kmsClient;
    private final String kmsKeyId;

    public AccountService(KmsClient kmsClient, String kmsKeyId) {
        this.kmsClient = kmsClient;
        this.kmsKeyId = kmsKeyId;
    }

    public Account createAccount(String customerId, AccountType accountType, Money initialBalance) {
        Account account = new Account(customerId, accountType, initialBalance);
        EncryptedAccount encryptedAccount = new EncryptedAccount(account, encryptData(account.getAccountId()));
        accounts.put(encryptedAccount.getEncryptedAccountId(), encryptedAccount);
        return account;
    }

    public Account getAccount(String accountId) {
        EncryptedAccount encryptedAccount = findAccountById(accountId);
        if (encryptedAccount == null) {
            throw new InvalidAccountException("Account not found: " + accountId);
        }
        return encryptedAccount.toAccount(decryptData(encryptedAccount.getEncryptedAccountId()));
    }
    
    private EncryptedAccount findAccountById(String accountId) {
        String encryptedAccountId = encryptData(accountId);
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
                customerAccounts.add(encryptedAccount.toAccount(decryptData(encryptedAccount.getEncryptedAccountId())));
            }
        }
        return customerAccounts;
    }

    public List<Account> getAllAccounts() {
        List<Account> allAccounts = new ArrayList<>();
        for (EncryptedAccount encryptedAccount : accounts.values()) {
            allAccounts.add(encryptedAccount.toAccount(decryptData(encryptedAccount.getEncryptedAccountId())));
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
    
    public void deposit(String accountId, Money amount) {
        EncryptedAccount encryptedAccount = findAccountByIdOrThrow(accountId);
        Account account = encryptedAccount.toAccount(decryptData(encryptedAccount.getEncryptedAccountId()));
        account.deposit(amount);
        encryptedAccount.updateBalance(account.getBalance());
    }
    
    public void withdraw(String accountId, Money amount) {
        EncryptedAccount encryptedAccount = findAccountByIdOrThrow(accountId);
        Account account = encryptedAccount.toAccount(decryptData(encryptedAccount.getEncryptedAccountId()));
        account.withdraw(amount);
        encryptedAccount.updateBalance(account.getBalance());
    }
    
    void updateAccountBalance(String accountId, Money newBalance) {
        EncryptedAccount encryptedAccount = findAccountByIdOrThrow(accountId);
        encryptedAccount.updateBalance(newBalance);
    }

    private String encryptData(String data) {
        try {
            EncryptRequest encryptRequest = EncryptRequest.builder()
                    .keyId(kmsKeyId)
                    .plaintext(SdkBytes.fromUtf8String(data))
                    .build();
            EncryptResponse encryptResponse = kmsClient.encrypt(encryptRequest);
            return Base64.getEncoder().encodeToString(encryptResponse.ciphertextBlob().asByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting data", e);
        }
    }

    private String decryptData(String encryptedData) {
        try {
            DecryptRequest decryptRequest = DecryptRequest.builder()
                    .ciphertextBlob(SdkBytes.fromByteArray(Base64.getDecoder().decode(encryptedData)))
                    .keyId(kmsKeyId)
                    .build();
            DecryptResponse decryptResponse = kmsClient.decrypt(decryptRequest);
            return decryptResponse.plaintext().asUtf8String();
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting data", e);
        }
    }
}