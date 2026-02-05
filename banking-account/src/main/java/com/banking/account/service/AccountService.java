package com.banking.account.service;

import com.banking.account.domain.Account;
import com.banking.account.domain.EncryptedAccount;
import com.banking.core.domain.AccountType;
import com.banking.core.domain.Money;
import com.banking.core.exception.InvalidAccountException;

import java.util.*;

public class AccountService {

    private final AccountRepository accountRepository;
    private final KmsClient kmsClient;
    private final String kmsKeyId;

    public AccountService(AccountRepository accountRepository, KmsClient kmsClient, String kmsKeyId) {
        this.accountRepository = accountRepository;
        this.kmsClient = kmsClient;
        this.kmsKeyId = kmsKeyId;
    }

    public Account createAccount(String customerId, AccountType accountType, Money initialBalance) {
        Account account = new Account(customerId, accountType, initialBalance);
        EncryptedAccount encryptedAccount = new EncryptedAccount(account.getAccountId(), account.getCustomerId(), account.getAccountType(), account.getBalance(), account.isActive(), encryptData(account.getAccountId()));
        Account createdAccount = accountRepository.save(encryptedAccount).toAccount(decryptData(encryptedAccount.getEncryptedAccountId()));
        return createdAccount;
    }

    public Account getAccount(String accountId) {
        EncryptedAccount encryptedAccount = accountRepository.findById(encryptData(accountId))
                .orElseThrow(() -> {
                    return new InvalidAccountException("Account not found: " + accountId);
                });
        Account account = encryptedAccount.toAccount(decryptData(encryptedAccount.getEncryptedAccountId()));
        return account;
    }

    public List<Account> getAccountsByCustomer(String customerId) {
        List<Account> customerAccounts = new ArrayList<>();
        for (EncryptedAccount encryptedAccount : accountRepository.findByCustomerId(customerId)) {
            customerAccounts.add(encryptedAccount.toAccount(decryptData(encryptedAccount.getEncryptedAccountId())));
        }
        return customerAccounts;
    }

    public List<Account> getAllAccounts() {
        List<Account> allAccounts = new ArrayList<>();
        for (EncryptedAccount encryptedAccount : accountRepository.findAll()) {
            allAccounts.add(encryptedAccount.toAccount(decryptData(encryptedAccount.getEncryptedAccountId())));
        }
        return allAccounts;
    }

    public void deactivateAccount(String accountId) {
        EncryptedAccount encryptedAccount = findAccountByIdOrThrow(accountId);
        encryptedAccount.deactivate();
        accountRepository.save(encryptedAccount);
    }

    public void activateAccount(String accountId) {
        EncryptedAccount encryptedAccount = findAccountByIdOrThrow(accountId);
        encryptedAccount.activate();
        accountRepository.save(encryptedAccount);
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
        accountRepository.save(encryptedAccount);
    }

    public void withdraw(String accountId, Money amount) {
        EncryptedAccount encryptedAccount = findAccountByIdOrThrow(accountId);
        Account account = encryptedAccount.toAccount(decryptData(encryptedAccount.getEncryptedAccountId()));
        account.withdraw(amount);
        encryptedAccount.updateBalance(account.getBalance());
        accountRepository.save(encryptedAccount);
    }

    void updateAccountBalance(String accountId, Money newBalance) {
        EncryptedAccount encryptedAccount = findAccountByIdOrThrow(accountId);
        encryptedAccount.updateBalance(newBalance);
        accountRepository.save(encryptedAccount);
    }

    private EncryptedAccount findAccountByIdOrThrow(String accountId) {
        return accountRepository.findById(encryptData(accountId))
                .orElseThrow(() -> {
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
            return encryptedData;
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
            String decryptedData = decryptResponse.plaintext().asUtf8String();
            return decryptedData;
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting data", e);
        }
    }
}