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
import com.banking.transaction.exception.InvalidInputException;
import com.banking.transaction.exception.TransactionNotFoundException;
import com.banking.transaction.util.InputValidator;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    private final AccountService accountService;
    private final TransactionRepository transactionRepository;
    private final InputValidator inputValidator;

    public TransactionService(AccountService accountService, TransactionRepository transactionRepository, InputValidator inputValidator) {
        if (accountService == null) {
            throw new IllegalArgumentException("AccountService cannot be null");
        }
        if (transactionRepository == null) {
            throw new IllegalArgumentException("TransactionRepository cannot be null");
        }
        if (inputValidator == null) {
            throw new IllegalArgumentException("InputValidator cannot be null");
        }
        this.accountService = accountService;
        this.transactionRepository = transactionRepository;
        this.inputValidator = inputValidator;
    }

    @Transactional
    public Transaction deposit(String accountId, Money amount, String description) throws InvalidInputException {
        try {
            validateInputs(accountId, amount, description);
            accountService.deposit(accountId, amount);
            Transaction transaction = new Transaction(accountId, TransactionType.DEPOSIT, amount, description);
            return transactionRepository.save(transaction);
        } catch (Exception e) {
            logger.error("Error during deposit operation", e);
            throw e;
        }
    }

    @Transactional
    public Transaction withdraw(String accountId, Money amount, String description) throws InvalidInputException, InsufficientFundsException {
        try {
            validateInputs(accountId, amount, description);
            accountService.withdraw(accountId, amount);
            Transaction transaction = new Transaction(accountId, TransactionType.WITHDRAWAL, amount, description);
            return transactionRepository.save(transaction);
        } catch (InsufficientFundsException e) {
            logger.error("Insufficient funds for withdrawal", e);
            throw e;
        } catch (Exception e) {
            logger.error("Error during withdrawal operation", e);
            throw e;
        }
    }

    @Transactional
    public Transaction transfer(String fromAccountId, String toAccountId, Money amount, String description) throws InvalidInputException, InsufficientFundsException {
        try {
            validateInputs(fromAccountId, amount, description);
            validateInputs(toAccountId, amount, description);
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
        } catch (InsufficientFundsException e) {
            logger.error("Insufficient funds for transfer", e);
            throw e;
        } catch (Exception e) {
            logger.error("Error during transfer operation", e);
            throw e;
        }
    }

    public List<Transaction> getTransactionsByAccount(String accountId) throws InvalidInputException {
        try {
            inputValidator.validateAccountId(accountId);
            return transactionRepository.findByAccountIdOrRelatedAccountId(accountId, accountId);
        } catch (Exception e) {
            logger.error("Error retrieving transactions for account", e);
            throw e;
        }
    }

    public Transaction getTransaction(String transactionId) throws InvalidInputException, TransactionNotFoundException {
        try {
            inputValidator.validateTransactionId(transactionId);
            return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found: " + transactionId));
        } catch (TransactionNotFoundException e) {
            logger.error("Transaction not found", e);
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving transaction", e);
            throw e;
        }
    }

    public List<Transaction> getAllTransactions() {
        try {
            return transactionRepository.findAll();
        } catch (Exception e) {
            logger.error("Error retrieving all transactions", e);
            throw e;
        }
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

    private void validateInputs(String accountId, Money amount, String description) throws InvalidInputException {
        inputValidator.validateAccountId(accountId);
        inputValidator.validateAmount(amount);
        inputValidator.validateDescription(description);
    }
}