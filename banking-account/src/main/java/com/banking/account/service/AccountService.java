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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final KmsClient kmsClient;
    private final String kmsKeyId;

    @Autowired
    public AccountService(AccountRepository accountRepository, KmsClient kmsClient, String kmsKeyId) {
        this.accountRepository = accountRepository;
        this.kmsClient = kmsClient;
        this.kmsKeyId = kmsKeyId;
    }

    @Transactional
    public Account createAccount(String customerId, AccountType accountType, Money initialBalance) {
        Account account = new Account(customerId, accountType, initialBalance);
        EncryptedAccount encryptedAccount = new EncryptedAccount(account, encryptData(account.getAccountId()));
        return accountRepository.save(encryptedAccount).toAccount(decryptData(encryptedAccount.getEncryptedAccountId()));
    }

    @Transactional(readOnly = true)
    public Account getAccount(String accountId) {
        EncryptedAccount encryptedAccount = accountRepository.findById(encryptData(accountId))
                .orElseThrow(() -> new InvalidAccountException("Account not found: " + accountId));
        return encryptedAccount.toAccount(decryptData(encryptedAccount.getEncryptedAccountId()));
    }

    @Transactional(readOnly = true)
    public List<Account> getAccountsByCustomer(String customerId) {
        List<Account> customerAccounts = new ArrayList<>();
        for (EncryptedAccount encryptedAccount : accountRepository.findByCustomerId(customerId)) {
            customerAccounts.add(encryptedAccount.toAccount(decryptData(encryptedAccount.getEncryptedAccountId())));
        }
        return customerAccounts;
    }

    @Transactional(readOnly = true)
    public List<Account> getAllAccounts() {
        List<Account> allAccounts = new ArrayList<>();
        for (EncryptedAccount encryptedAccount : accountRepository.findAll()) {
            allAccounts.add(encryptedAccount.toAccount(decryptData(encryptedAccount.getEncryptedAccountId())));
        }
        return allAccounts;
    }

    @Transactional
    public void deactivateAccount(String accountId) {
        EncryptedAccount encryptedAccount = findAccountByIdOrThrow(accountId);
        encryptedAccount.deactivate();
        accountRepository.save(encryptedAccount);
    }

    @Transactional
    public void activateAccount(String accountId) {
        EncryptedAccount encryptedAccount = findAccountByIdOrThrow(accountId);
        encryptedAccount.activate();
        accountRepository.save(encryptedAccount);
    }

    @Transactional(readOnly = true)
    public Money getBalance(String accountId) {
        Account account = getAccount(accountId);
        return account.getBalance();
    }

    @Transactional
    public void deposit(String accountId, Money amount) {
        EncryptedAccount encryptedAccount = findAccountByIdOrThrow(accountId);
        Account account = encryptedAccount.toAccount(decryptData(encryptedAccount.getEncryptedAccountId()));
        account.deposit(amount);
        encryptedAccount.updateBalance(account.getBalance());
        accountRepository.save(encryptedAccount);
    }

    @Transactional
    public void withdraw(String accountId, Money amount) {
        EncryptedAccount encryptedAccount = findAccountByIdOrThrow(accountId);
        Account account = encryptedAccount.toAccount(decryptData(encryptedAccount.getEncryptedAccountId()));
        account.withdraw(amount);
        encryptedAccount.updateBalance(account.getBalance());
        accountRepository.save(encryptedAccount);
    }

    @Transactional
    void updateAccountBalance(String accountId, Money newBalance) {
        EncryptedAccount encryptedAccount = findAccountByIdOrThrow(accountId);
        encryptedAccount.updateBalance(newBalance);
        accountRepository.save(encryptedAccount);
    }

    private EncryptedAccount findAccountByIdOrThrow(String accountId) {
        return accountRepository.findById(encryptData(accountId))
                .orElseThrow(() -> new InvalidAccountException("Account not found: " + accountId));
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