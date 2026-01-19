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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AccountService {
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

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
        logger.info("Creating account for customer: {}, type: {}, initial balance: {}", customerId, accountType, initialBalance);
        Account account = new Account(customerId, accountType, initialBalance);
        EncryptedAccount encryptedAccount = new EncryptedAccount(account, encryptData(account.getAccountId()));
        Account createdAccount = accountRepository.save(encryptedAccount).toAccount(decryptData(encryptedAccount.getEncryptedAccountId()));
        logger.info("Account created successfully: {}", createdAccount.getAccountId());
        return createdAccount;
    }

    @Transactional(readOnly = true)
    public Account getAccount(String accountId) {
        logger.info("Retrieving account: {}", accountId);
        EncryptedAccount encryptedAccount = accountRepository.findById(encryptData(accountId))
                .orElseThrow(() -> {
                    logger.warn("Account not found: {}", accountId);
                    return new InvalidAccountException("Account not found: " + accountId);
                });
        Account account = encryptedAccount.toAccount(decryptData(encryptedAccount.getEncryptedAccountId()));
        logger.info("Account retrieved successfully: {}", accountId);
        return account;
    }

    @Transactional(readOnly = true)
    public List<Account> getAccountsByCustomer(String customerId) {
        logger.info("Retrieving accounts for customer: {}", customerId);
        List<Account> customerAccounts = new ArrayList<>();
        for (EncryptedAccount encryptedAccount : accountRepository.findByCustomerId(customerId)) {
            customerAccounts.add(encryptedAccount.toAccount(decryptData(encryptedAccount.getEncryptedAccountId())));
        }
        logger.info("Retrieved {} accounts for customer: {}", customerAccounts.size(), customerId);
        return customerAccounts;
    }

    @Transactional(readOnly = true)
    public List<Account> getAllAccounts() {
        logger.info("Retrieving all accounts");
        List<Account> allAccounts = new ArrayList<>();
        for (EncryptedAccount encryptedAccount : accountRepository.findAll()) {
            allAccounts.add(encryptedAccount.toAccount(decryptData(encryptedAccount.getEncryptedAccountId())));
        }
        logger.info("Retrieved {} accounts in total", allAccounts.size());
        return allAccounts;
    }

    @Transactional
    public void deactivateAccount(String accountId) {
        logger.info("Deactivating account: {}", accountId);
        EncryptedAccount encryptedAccount = findAccountByIdOrThrow(accountId);
        encryptedAccount.deactivate();
        accountRepository.save(encryptedAccount);
        logger.info("Account deactivated successfully: {}", accountId);
    }

    @Transactional
    public void activateAccount(String accountId) {
        logger.info("Activating account: {}", accountId);
        EncryptedAccount encryptedAccount = findAccountByIdOrThrow(accountId);
        encryptedAccount.activate();
        accountRepository.save(encryptedAccount);
        logger.info("Account activated successfully: {}", accountId);
    }

    @Transactional(readOnly = true)
    public Money getBalance(String accountId) {
        logger.info("Retrieving balance for account: {}", accountId);
        Account account = getAccount(accountId);
        logger.info("Balance retrieved for account {}: {}", accountId, account.getBalance());
        return account.getBalance();
    }

    @Transactional
    public void deposit(String accountId, Money amount) {
        logger.info("Depositing {} to account: {}", amount, accountId);
        EncryptedAccount encryptedAccount = findAccountByIdOrThrow(accountId);
        Account account = encryptedAccount.toAccount(decryptData(encryptedAccount.getEncryptedAccountId()));
        account.deposit(amount);
        encryptedAccount.updateBalance(account.getBalance());
        accountRepository.save(encryptedAccount);
        logger.info("Deposit successful for account {}: {}", accountId, amount);
    }

    @Transactional
    public void withdraw(String accountId, Money amount) {
        logger.info("Withdrawing {} from account: {}", amount, accountId);
        EncryptedAccount encryptedAccount = findAccountByIdOrThrow(accountId);
        Account account = encryptedAccount.toAccount(decryptData(encryptedAccount.getEncryptedAccountId()));
        account.withdraw(amount);
        encryptedAccount.updateBalance(account.getBalance());
        accountRepository.save(encryptedAccount);
        logger.info("Withdrawal successful for account {}: {}", accountId, amount);
    }

    @Transactional
    void updateAccountBalance(String accountId, Money newBalance) {
        logger.info("Updating balance for account {}: {}", accountId, newBalance);
        EncryptedAccount encryptedAccount = findAccountByIdOrThrow(accountId);
        encryptedAccount.updateBalance(newBalance);
        accountRepository.save(encryptedAccount);
        logger.info("Balance updated successfully for account {}: {}", accountId, newBalance);
    }

    private EncryptedAccount findAccountByIdOrThrow(String accountId) {
        return accountRepository.findById(encryptData(accountId))
                .orElseThrow(() -> {
                    logger.warn("Account not found: {}", accountId);
                    return new InvalidAccountException("Account not found: " + accountId);
                });
    }

    private String encryptData(String data) {
        try {
            EncryptRequest encryptRequest = EncryptRequest.builder()
                    .keyId(kmsKeyId)
                    .plaintext(SdkBytes.fromUtf8String(data))
                    .build();
            EncryptResponse encryptResponse = kmsClient.encrypt(encryptRequest);
            String encryptedData = Base64.getEncoder().encodeToString(encryptResponse.ciphertextBlob().asByteArray());
            logger.debug("Data encrypted successfully");
            return encryptedData;
        } catch (Exception e) {
            logger.error("Error encrypting data", e);
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
            String decryptedData = decryptResponse.plaintext().asUtf8String();
            logger.debug("Data decrypted successfully");
            return decryptedData;
        } catch (Exception e) {
            logger.error("Error decrypting data", e);
            throw new RuntimeException("Error decrypting data", e);
        }
    }
}