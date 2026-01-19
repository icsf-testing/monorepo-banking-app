================================================================================
FIXED CODE FOR: banking-transaction/src/main/java/com/banking/transaction/service/TransactionService.java
================================================================================
package com.banking.transaction.service;

import com.banking.account.domain.Account;
import com.banking.account.service.AccountService;
import com.banking.core.domain.Money;
import com.banking.core.domain.TransactionType;
import com.banking.core.exception.InsufficientFundsException;
import com.banking.transaction.domain.Transaction;
import com.banking.transaction.repository.TransactionRepository;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

public class TransactionService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    private final AccountService accountService;
    private final TransactionRepository transactionRepository;

    public TransactionService(AccountService accountService, TransactionRepository transactionRepository) {
        if (accountService == null) {
            throw new IllegalArgumentException("AccountService cannot be null");
        }
        if (transactionRepository == null) {
            throw new IllegalArgumentException("TransactionRepository cannot be null");
        }
        this.accountService = accountService;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Transaction deposit(String accountId, Money amount, String description) {
        accountService.deposit(accountId, amount);
        Transaction transaction = new Transaction(accountId, TransactionType.DEPOSIT, amount, description);
        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction withdraw(String accountId, Money amount, String description) {
        accountService.withdraw(accountId, amount);
        Transaction transaction = new Transaction(accountId, TransactionType.WITHDRAWAL, amount, description);
        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction transfer(String fromAccountId, String toAccountId, Money amount, String description) {
        accountService.withdraw(fromAccountId, amount);
        accountService.deposit(toAccountId, amount);

        Transaction transaction = new Transaction(
            fromAccountId, 
            TransactionType.TRANSFER, 
            amount, 
            description,
            toAccountId
        );
        return transactionRepository.save(transaction);
    }

    public List<Transaction> getTransactionsByAccount(String accountId) {
        return transactionRepository.findByAccountIdOrRelatedAccountId(accountId, accountId);
    }

    public Transaction getTransaction(String transactionId) {
        return transactionRepository.findById(transactionId)
            .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public Money calculateAccountBalance(String accountId) throws InvalidInputException {
        try {
            inputValidator.validateAccountId(accountId);
            Account account = accountService.getAccount(accountId);
            return account.getBalance();
        } catch (Exception e) {
            logger.error("Error calculating account balance", e);
            throw e;
        }
    }
}