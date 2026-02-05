Here's a complete JUnit 5 test class for the TransactionType enum:

```java
package com.banking.core.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTypeTest {

    @Test
    void testEnumValues() {
        TransactionType[] expectedTypes = {
            TransactionType.DEPOSIT,
            TransactionType.WITHDRAWAL,
            TransactionType.TRANSFER,
            TransactionType.INTEREST_CREDIT
        };
        
        assertArrayEquals(expectedTypes, TransactionType.values());
    }

    @ParameterizedTest
    @EnumSource(TransactionType.class)
    void testEnumValuesExist(TransactionType transactionType) {
        assertNotNull(transactionType);
    }

    @Test
    void testEnumValuesCount() {
        assertEquals(4, TransactionType.values().length);
    }

    @Test
    void testEnumValueOf() {
        assertEquals(TransactionType.DEPOSIT, TransactionType.valueOf("DEPOSIT"));
        assertEquals(TransactionType.WITHDRAWAL, TransactionType.valueOf("WITHDRAWAL"));
        assertEquals(TransactionType.TRANSFER, TransactionType.valueOf("TRANSFER"));
        assertEquals(TransactionType.INTEREST_CREDIT, TransactionType.valueOf("INTEREST_CREDIT"));
    }

    @Test
    void testEnumValueOfIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> TransactionType.valueOf("INVALID_TYPE"));
    }

    @Test
    void testEnumValueOfNullArgument() {
        assertThrows(NullPointerException.class, () -> TransactionType.valueOf(null));
    }

    @Test
    void testEnumOrdinal() {
        assertEquals(0, TransactionType.DEPOSIT.ordinal());
        assertEquals(1, TransactionType.WITHDRAWAL.ordinal());
        assertEquals(2, TransactionType.TRANSFER.ordinal());
        assertEquals(3, TransactionType.INTEREST_CREDIT.ordinal());
    }

    @Test
    void testEnumToString() {
        assertEquals("DEPOSIT", TransactionType.DEPOSIT.toString());
        assertEquals("WITHDRAWAL", TransactionType.WITHDRAWAL.toString());
        assertEquals("TRANSFER", TransactionType.TRANSFER.toString());
        assertEquals("INTEREST_CREDIT", TransactionType.INTEREST_CREDIT.toString());
    }
}
```
