package com.banking.transaction.service;

import com.banking.account.domain.Account;
import com.banking.account.service.AccountService;
import com.banking.core.domain.Money;
import com.banking.core.domain.TransactionType;
import com.banking.core.exception.InsufficientFundsException;
import com.banking.transaction.domain.Transaction;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service for processing banking transactions.
 * 
 * SECURITY NOTICE: This is a Proof of Concept (PoC) implementation.
 * Transaction data is stored in-memory for demonstration purposes only.
 * 
 * PRODUCTION CONSIDERATIONS:
 * - Use a persistent database with transaction logging and audit trails
 * - Implement encryption at rest for sensitive transaction data
 * - Ensure ACID compliance for financial transactions
 * - Implement proper access controls and audit logging
 * - Follow PCI DSS, SOX, and banking security regulations
 * - Use database transactions to ensure data consistency
 * 
 * For production use, replace the in-memory storage with a proper persistence layer.
 */
public class TransactionService {
    private final AccountService accountService;
    // SECURITY: In-memory storage - for PoC/demo only, not suitable for production
    // TODO: Replace with persistent database storage with encryption for production
    private final Map<String, Transaction> transactions = new ConcurrentHashMap<>();

    public TransactionService(AccountService accountService) {
        if (accountService == null) {
            throw new IllegalArgumentException("AccountService cannot be null");
        }
        this.accountService = accountService;
    }

    public Transaction deposit(String accountId, Money amount, String description) {
        // Use AccountService methods to ensure encrypted storage is updated
        accountService.deposit(accountId, amount);
        Transaction transaction = new Transaction(accountId, TransactionType.DEPOSIT, amount, description);
        transactions.put(transaction.getTransactionId(), transaction);
        return transaction;
    }

    public Transaction withdraw(String accountId, Money amount, String description) {
        // Use AccountService methods to ensure encrypted storage is updated
        accountService.withdraw(accountId, amount);
        Transaction transaction = new Transaction(accountId, TransactionType.WITHDRAWAL, amount, description);
        transactions.put(transaction.getTransactionId(), transaction);
        return transaction;
    }

    public Transaction transfer(String fromAccountId, String toAccountId, Money amount, String description) {
        // Use AccountService methods to ensure encrypted storage is updated
        accountService.withdraw(fromAccountId, amount);
        accountService.deposit(toAccountId, amount);

        // Create transaction record
        Transaction transaction = new Transaction(
            fromAccountId, 
            TransactionType.TRANSFER, 
            amount, 
            description,
            toAccountId
        );
        transactions.put(transaction.getTransactionId(), transaction);
        return transaction;
    }

    public List<Transaction> getTransactionsByAccount(String accountId) {
        return transactions.values().stream()
                .filter(t -> t.getAccountId().equals(accountId) || 
                           (t.getRelatedAccountId() != null && t.getRelatedAccountId().equals(accountId)))
                .sorted(Comparator.comparing(Transaction::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    public Transaction getTransaction(String transactionId) {
        Transaction transaction = transactions.get(transactionId);
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction not found: " + transactionId);
        }
        return transaction;
    }

    public List<Transaction> getAllTransactions() {
        return new ArrayList<>(transactions.values());
    }

    public Money calculateAccountBalance(String accountId) {
        Account account = accountService.getAccount(accountId);
        return account.getBalance();
    }
}

