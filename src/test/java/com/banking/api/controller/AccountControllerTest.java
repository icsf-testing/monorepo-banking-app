Here's a JUnit 5 test class for the AccountController:

```java
package com.banking.api.controller;

import com.banking.account.domain.Account;
import com.banking.account.service.AccountService;
import com.banking.api.dto.AccountCreateRequest;
import com.banking.api.dto.AccountResponse;
import com.banking.core.domain.AccountType;
import com.banking.core.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountControllerTest {

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountController accountController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createAccount_ShouldReturnCreatedAccount() {
        // Arrange
        AccountCreateRequest request = new AccountCreateRequest();
        request.setCustomerId("CUST123");
        request.setAccountType("SAVINGS");
        request.setInitialBalance(1000.0);
        request.setCurrency("USD");

        Account createdAccount = new Account("ACC123", "CUST123", AccountType.SAVINGS, new Money(BigDecimal.valueOf(1000.0), "USD"), true);
        when(accountService.createAccount(anyString(), any(AccountType.class), any(Money.class))).thenReturn(createdAccount);

        // Act
        ResponseEntity<AccountResponse> response = accountController.createAccount(request);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ACC123", response.getBody().getAccountId());
        assertEquals("CUST123", response.getBody().getCustomerId());
        assertEquals("SAVINGS", response.getBody().getAccountType());
        assertEquals(1000.0, response.getBody().getBalance(), 0.001);
        assertEquals("USD", response.getBody().getCurrency());
        assertTrue(response.getBody().isActive());
    }

    @Test
    void getAccount_ShouldReturnAccount() {
        // Arrange
        String accountId = "ACC123";
        Account account = new Account(accountId, "CUST123", AccountType.CHECKING, new Money(BigDecimal.valueOf(500.0), "EUR"), true);
        when(accountService.getAccount(accountId)).thenReturn(account);

        // Act
        ResponseEntity<AccountResponse> response = accountController.getAccount(accountId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(accountId, response.getBody().getAccountId());
        assertEquals("CHECKING", response.getBody().getAccountType());
        assertEquals(500.0, response.getBody().getBalance(), 0.001);
        assertEquals("EUR", response.getBody().getCurrency());
    }

    @Test
    void getAccountsByCustomer_ShouldReturnListOfAccounts() {
        // Arrange
        String customerId = "CUST123";
        List<Account> accounts = Arrays.asList(
            new Account("ACC1", customerId, AccountType.SAVINGS, new Money(BigDecimal.valueOf(1000.0), "USD"), true),
            new Account("ACC2", customerId, AccountType.CHECKING, new Money(BigDecimal.valueOf(500.0), "USD"), true)
        );
        when(accountService.getAccountsByCustomer(customerId)).thenReturn(accounts);

        // Act
        ResponseEntity<List<AccountResponse>> response = accountController.getAccountsByCustomer(customerId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("ACC1", response.getBody().get(0).getAccountId());
        assertEquals("ACC2", response.getBody().get(1).getAccountId());
    }

    @Test
    void getAllAccounts_ShouldReturnListOfAllAccounts() {
        // Arrange
        List<Account> accounts = Arrays.asList(
            new Account("ACC1", "CUST1", AccountType.SAVINGS, new Money(BigDecimal.valueOf(1000.0), "USD"), true),
            new Account("ACC2", "CUST2", AccountType.CHECKING, new Money(BigDecimal.valueOf(500.0), "EUR"), true)
        );
        when(accountService.getAllAccounts()).thenReturn(accounts);

        // Act
        ResponseEntity<List<AccountResponse>> response = accountController.getAllAccounts();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void getBalance_ShouldReturnAccountBalance() {
        // Arrange
        String accountId = "ACC123";
        Money balance = new Money(BigDecimal.valueOf(1500.0), "USD");
        when(accountService.getBalance(accountId)).thenReturn(balance);

        // Act
        ResponseEntity<Money> response = accountController.getBalance(accountId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1500.0, response.getBody().getAmount().doubleValue(), 0.001);
        assertEquals("USD", response.getBody().getCurrency());
    }

    @Test
    void createAccount_WithInvalidAccountType_ShouldThrowException() {
        // Arrange
        AccountCreateRequest request = new AccountCreateRequest();
        request.setCustomerId("CUST123");
        request.setAccountType("INVALID_TYPE");
        request.setInitialBalance(1000.0);
        request.setCurrency("USD");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> accountController.createAccount(request));
    }

    @Test
    void getAccount_WithNonExistentId_ShouldReturnNotFound() {
        // Arrange
        String nonExistentId = "NON_EXISTENT";
        when(accountService.getAccount(nonExistentId)).thenThrow(new RuntimeException("Account not found"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> accountController.getAccount(nonExistentId));
    }
}
```
