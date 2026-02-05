package com.banking.api.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class TransactionResponseTest {

    @Test
    void testTransactionResponseDefaultConstructor() {
        TransactionResponse response = new TransactionResponse();
        assertNull(response.getTransactionId());
        assertNull(response.getAccountId());
        assertNull(response.getType());
        assertEquals(0.0, response.getAmount(), 0.001);
        assertNull(response.getCurrency());
        assertNull(response.getTimestamp());
        assertNull(response.getDescription());
        assertNull(response.getRelatedAccountId());
    }

    @Test
    void testTransactionIdGetterAndSetter() {
        TransactionResponse response = new TransactionResponse();
        String transactionId = "TX123456";
        response.setTransactionId(transactionId);
        assertEquals(transactionId, response.getTransactionId());
    }

    @Test
    void testAccountIdGetterAndSetter() {
        TransactionResponse response = new TransactionResponse();
        String accountId = "ACC987654";
        response.setAccountId(accountId);
        assertEquals(accountId, response.getAccountId());
    }

    @ParameterizedTest
    @ValueSource(strings = {"DEPOSIT", "WITHDRAWAL", "TRANSFER"})
    void testTypeGetterAndSetter(String type) {
        TransactionResponse response = new TransactionResponse();
        response.setType(type);
        assertEquals(type, response.getType());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, 100.50, 1000000.99, -50.25})
    void testAmountGetterAndSetter(double amount) {
        TransactionResponse response = new TransactionResponse();
        response.setAmount(amount);
        assertEquals(amount, response.getAmount(), 0.001);
    }

    @Test
    void testCurrencyGetterAndSetter() {
        TransactionResponse response = new TransactionResponse();
        String currency = "USD";
        response.setCurrency(currency);
        assertEquals(currency, response.getCurrency());
    }

    @Test
    void testTimestampGetterAndSetter() {
        TransactionResponse response = new TransactionResponse();
        String timestamp = "2023-04-01T10:30:00Z";
        response.setTimestamp(timestamp);
        assertEquals(timestamp, response.getTimestamp());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"Payment for services", "Monthly rent", "Salary deposit"})
    void testDescriptionGetterAndSetter(String description) {
        TransactionResponse response = new TransactionResponse();
        response.setDescription(description);
        assertEquals(description, response.getDescription());
    }

    @Test
    void testRelatedAccountIdGetterAndSetter() {
        TransactionResponse response = new TransactionResponse();
        String relatedAccountId = "ACC555666";
        response.setRelatedAccountId(relatedAccountId);
        assertEquals(relatedAccountId, response.getRelatedAccountId());
    }

    @Test
    void testAllFieldsSetAndGet() {
        TransactionResponse response = new TransactionResponse();
        response.setTransactionId("TX987");
        response.setAccountId("ACC123");
        response.setType("TRANSFER");
        response.setAmount(500.75);
        response.setCurrency("EUR");
        response.setTimestamp("2023-04-02T15:45:30Z");
        response.setDescription("Fund transfer");
        response.setRelatedAccountId("ACC456");

        assertEquals("TX987", response.getTransactionId());
        assertEquals("ACC123", response.getAccountId());
        assertEquals("TRANSFER", response.getType());
        assertEquals(500.75, response.getAmount(), 0.001);
        assertEquals("EUR", response.getCurrency());
        assertEquals("2023-04-02T15:45:30Z", response.getTimestamp());
        assertEquals("Fund transfer", response.getDescription());
        assertEquals("ACC456", response.getRelatedAccountId());
    }
}
