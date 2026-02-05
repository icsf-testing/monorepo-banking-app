Here's a JUnit 5 test class for the TransactionRequest class:

```java
package com.banking.api.dto;

import com.banking.api.service.KeyManagementService;
import com.banking.api.service.TokenizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionRequestTest {

    private TransactionRequest transactionRequest;
    private TokenizationService tokenizationService;
    private KeyManagementService keyManagementService;
    private Validator validator;

    @BeforeEach
    void setUp() {
        tokenizationService = mock(TokenizationService.class);
        keyManagementService = mock(KeyManagementService.class);
        transactionRequest = new TransactionRequest(tokenizationService, keyManagementService);

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidTransactionRequest() {
        transactionRequest.setAccountIdToken("validToken22chars12345");
        transactionRequest.setFromAccountIdToken("validToken22chars12345");
        transactionRequest.setToAccountIdToken("validToken22chars12345");
        transactionRequest.setAmount(100.0);
        transactionRequest.setCurrency("USD");
        transactionRequest.setDescription("Valid description");

        Set<ConstraintViolation<TransactionRequest>> violations = validator.validate(transactionRequest);
        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "invalidToken", "tooShortToken", "tooLongTokenMoreThan22Chars"})
    void testInvalidAccountIdToken(String invalidToken) {
        transactionRequest.setAccountIdToken(invalidToken);
        Set<ConstraintViolation<TransactionRequest>> violations = validator.validate(transactionRequest);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testNegativeAmount() {
        transactionRequest.setAmount(-100.0);
        Set<ConstraintViolation<TransactionRequest>> violations = validator.validate(transactionRequest);
        assertFalse(violations.isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "USDD", "US"})
    void testInvalidCurrency(String invalidCurrency) {
        transactionRequest.setCurrency(invalidCurrency);
        Set<ConstraintViolation<TransactionRequest>> violations = validator.validate(transactionRequest);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testDescriptionTooLong() {
        transactionRequest.setDescription("A".repeat(256));
        Set<ConstraintViolation<TransactionRequest>> violations = validator.validate(transactionRequest);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testSanitizeInput() {
        String unsafeInput = "<script>alert('XSS')</script>";
        transactionRequest.setDescription(unsafeInput);
        assertNotEquals(unsafeInput, transactionRequest.getDescription());
        assertFalse(transactionRequest.getDescription().contains("<script>"));
    }

    @Test
    void testDetokenizeAccountId() {
        String token = "validToken22chars12345";
        String detokenized = "12345";
        when(tokenizationService.detokenize(token)).thenReturn(detokenized);
        when(keyManagementService.getTokenHash(detokenized)).thenReturn("hash");

        String result = transactionRequest.detokenizeAccountId(token);

        assertEquals(detokenized, result);
        verify(tokenizationService).detokenize(token);
        verify(keyManagementService).getTokenHash(detokenized);
    }

    @Test
    void testDetokenizeAccountIdWithInvalidToken() {
        assertThrows(IllegalArgumentException.class, () -> transactionRequest.detokenizeAccountId("invalidToken"));
    }

    @Test
    void testDetokenizeAccountIdWithIntegrityCheckFailure() {
        String token = "validToken22chars12345";
        String detokenized = "12345";
        when(tokenizationService.detokenize(token)).thenReturn(detokenized);
        when(keyManagementService.getTokenHash(detokenized)).thenReturn("expectedHash");

        assertThrows(RuntimeException.class, () -> transactionRequest.detokenizeAccountId(token));
    }
}
```
