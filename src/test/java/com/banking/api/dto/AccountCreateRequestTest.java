Here's a complete JUnit 5 test class for the AccountCreateRequest class:

```java
package com.banking.api.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class AccountCreateRequestTest {

    private AccountCreateRequest request;

    @BeforeEach
    void setUp() {
        request = new AccountCreateRequest();
    }

    @Test
    void testDefaultConstructor() {
        assertNull(request.getCustomerId());
        assertNull(request.getAccountType());
        assertEquals(0.0, request.getInitialBalance(), 0.001);
        assertNull(request.getCurrency());
    }

    @Test
    void testCustomerIdGetterAndSetter() {
        String customerId = "CUST123456";
        request.setCustomerId(customerId);
        assertEquals(customerId, request.getCustomerId());
    }

    @ParameterizedTest
    @ValueSource(strings = {"SAVINGS", "CHECKING", "CURRENT", "FIXED_DEPOSIT"})
    void testAccountTypeGetterAndSetter(String accountType) {
        request.setAccountType(accountType);
        assertEquals(accountType, request.getAccountType());
    }

    @Test
    void testInitialBalanceGetterAndSetter() {
        double initialBalance = 1000.50;
        request.setInitialBalance(initialBalance);
        assertEquals(initialBalance, request.getInitialBalance(), 0.001);
    }

    @Test
    void testInitialBalanceWithZero() {
        request.setInitialBalance(0.0);
        assertEquals(0.0, request.getInitialBalance(), 0.001);
    }

    @Test
    void testInitialBalanceWithNegativeValue() {
        double negativeBalance = -500.75;
        request.setInitialBalance(negativeBalance);
        assertEquals(negativeBalance, request.getInitialBalance(), 0.001);
    }

    @ParameterizedTest
    @ValueSource(strings = {"USD", "EUR", "GBP", "JPY"})
    void testCurrencyGetterAndSetter(String currency) {
        request.setCurrency(currency);
        assertEquals(currency, request.getCurrency());
    }

    @Test
    void testSetAndGetAllFields() {
        String customerId = "CUST987654";
        String accountType = "SAVINGS";
        double initialBalance = 5000.75;
        String currency = "EUR";

        request.setCustomerId(customerId);
        request.setAccountType(accountType);
        request.setInitialBalance(initialBalance);
        request.setCurrency(currency);

        assertEquals(customerId, request.getCustomerId());
        assertEquals(accountType, request.getAccountType());
        assertEquals(initialBalance, request.getInitialBalance(), 0.001);
        assertEquals(currency, request.getCurrency());
    }

    @Test
    void testSetCustomerIdToNull() {
        request.setCustomerId(null);
        assertNull(request.getCustomerId());
    }

    @Test
    void testSetAccountTypeToNull() {
        request.setAccountType(null);
        assertNull(request.getAccountType());
    }

    @Test
    void testSetCurrencyToNull() {
        request.setCurrency(null);
        assertNull(request.getCurrency());
    }

    @Test
    void testSetCustomerIdToEmptyString() {
        request.setCustomerId("");
        assertEquals("", request.getCustomerId());
    }

    @Test
    void testSetAccountTypeToEmptyString() {
        request.setAccountType("");
        assertEquals("", request.getAccountType());
    }

    @Test
    void testSetCurrencyToEmptyString() {
        request.setCurrency("");
        assertEquals("", request.getCurrency());
    }
}
```
