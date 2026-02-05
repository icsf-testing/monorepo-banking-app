Here's a JUnit 5 test class for the AccountType enum:

```java
package com.banking.core.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class AccountTypeTest {

    @Test
    void testEnumValues() {
        AccountType[] expectedTypes = {
            AccountType.SAVINGS,
            AccountType.CHECKING,
            AccountType.CURRENT,
            AccountType.FIXED_DEPOSIT
        };
        
        assertArrayEquals(expectedTypes, AccountType.values());
    }

    @ParameterizedTest
    @EnumSource(AccountType.class)
    void testEnumValuesExist(AccountType accountType) {
        assertNotNull(accountType);
    }

    @Test
    void testEnumValuesCount() {
        assertEquals(4, AccountType.values().length);
    }

    @Test
    void testEnumValueOf() {
        assertEquals(AccountType.SAVINGS, AccountType.valueOf("SAVINGS"));
        assertEquals(AccountType.CHECKING, AccountType.valueOf("CHECKING"));
        assertEquals(AccountType.CURRENT, AccountType.valueOf("CURRENT"));
        assertEquals(AccountType.FIXED_DEPOSIT, AccountType.valueOf("FIXED_DEPOSIT"));
    }

    @Test
    void testEnumValueOfIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> AccountType.valueOf("INVALID_TYPE"));
    }

    @Test
    void testEnumValueOfNullArgument() {
        assertThrows(NullPointerException.class, () -> AccountType.valueOf(null));
    }

    @Test
    void testEnumToString() {
        assertEquals("SAVINGS", AccountType.SAVINGS.toString());
        assertEquals("CHECKING", AccountType.CHECKING.toString());
        assertEquals("CURRENT", AccountType.CURRENT.toString());
        assertEquals("FIXED_DEPOSIT", AccountType.FIXED_DEPOSIT.toString());
    }

    @Test
    void testEnumOrdinal() {
        assertEquals(0, AccountType.SAVINGS.ordinal());
        assertEquals(1, AccountType.CHECKING.ordinal());
        assertEquals(2, AccountType.CURRENT.ordinal());
        assertEquals(3, AccountType.FIXED_DEPOSIT.ordinal());
    }

    @Test
    void testEnumCompareTo() {
        assertTrue(AccountType.SAVINGS.compareTo(AccountType.CHECKING) < 0);
        assertTrue(AccountType.CHECKING.compareTo(AccountType.CURRENT) < 0);
        assertTrue(AccountType.CURRENT.compareTo(AccountType.FIXED_DEPOSIT) < 0);
        assertEquals(0, AccountType.SAVINGS.compareTo(AccountType.SAVINGS));
    }
}
```
