package com.banking.api.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class AccountResponseTest {

    private AccountResponse accountResponse;

    @BeforeEach
    void setUp() {
        accountResponse = new AccountResponse();
    }

    @Test
    void testDefaultConstructor() {
        assertNull(accountResponse.getAccountId());
        assertNull(accountResponse.getCustomerId());
        assertNull(accountResponse.getAccountType());
        assertEquals(0.0, accountResponse.getBalance(), 0.001);
        assertNull(accountResponse.getCurrency());
        assertFalse(accountResponse.isActive());
    }

    @Test
    void testAccountIdGetterAndSetter() {
        String accountId = "ACC123456";
        accountResponse.setAccountId(accountId);
        assertEquals(accountId, accountResponse.getAccountId());
    }

    @Test
    void testCustomerIdGetterAndSetter() {
        String customerId = "CUST789012";
        accountResponse.setCustomerId(customerId);
        assertEquals(customerId, accountResponse.getCustomerId());
    }

    @ParameterizedTest
    @ValueSource(strings = {"SAVINGS", "CHECKING", "CURRENT", "FIXED_DEPOSIT"})
    void testAccountTypeGetterAndSetter(String accountType) {
        accountResponse.setAccountType(accountType);
        assertEquals(accountType, accountResponse.getAccountType());
    }

    @Test
    void testBalanceGetterAndSetter() {
        double balance = 1000.50;
        accountResponse.setBalance(balance);
        assertEquals(balance, accountResponse.getBalance(), 0.001);
    }

    @Test
    void testNegativeBalance() {
        double negativeBalance = -500.75;
        accountResponse.setBalance(negativeBalance);
        assertEquals(negativeBalance, accountResponse.getBalance(), 0.001);
    }

    @ParameterizedTest
    @ValueSource(strings = {"USD", "EUR", "GBP", "JPY"})
    void testCurrencyGetterAndSetter(String currency) {
        accountResponse.setCurrency(currency);
        assertEquals(currency, accountResponse.getCurrency());
    }

    @Test
    void testActiveGetterAndSetter() {
        accountResponse.setActive(true);
        assertTrue(accountResponse.isActive());

        accountResponse.setActive(false);
        assertFalse(accountResponse.isActive());
    }

    @Test
    void testNullAccountId() {
        accountResponse.setAccountId(null);
        assertNull(accountResponse.getAccountId());
    }

    @Test
    void testEmptyAccountId() {
        accountResponse.setAccountId("");
        assertEquals("", accountResponse.getAccountId());
    }

    @Test
    void testNullCustomerId() {
        accountResponse.setCustomerId(null);
        assertNull(accountResponse.getCustomerId());
    }

    @Test
    void testEmptyCustomerId() {
        accountResponse.setCustomerId("");
        assertEquals("", accountResponse.getCustomerId());
    }

    @Test
    void testNullAccountType() {
        accountResponse.setAccountType(null);
        assertNull(accountResponse.getAccountType());
    }

    @Test
    void testEmptyAccountType() {
        accountResponse.setAccountType("");
        assertEquals("", accountResponse.getAccountType());
    }

    @Test
    void testZeroBalance() {
        accountResponse.setBalance(0.0);
        assertEquals(0.0, accountResponse.getBalance(), 0.001);
    }

    @Test
    void testNullCurrency() {
        accountResponse.setCurrency(null);
        assertNull(accountResponse.getCurrency());
    }

    @Test
    void testEmptyCurrency() {
        accountResponse.setCurrency("");
        assertEquals("", accountResponse.getCurrency());
    }
}
