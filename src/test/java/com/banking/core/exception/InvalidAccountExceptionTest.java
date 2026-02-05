Here's a JUnit 5 test class for the InvalidAccountException:

```java
package com.banking.core.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class InvalidAccountExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String errorMessage = "Invalid account number";
        InvalidAccountException exception = new InvalidAccountException(errorMessage);
        
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void testExceptionIsRuntimeException() {
        InvalidAccountException exception = new InvalidAccountException("Test");
        
        assertTrue(exception instanceof RuntimeException);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void testConstructorWithNullEmptyOrBlankMessage(String message) {
        InvalidAccountException exception = new InvalidAccountException(message);
        
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testExceptionWithLongMessage() {
        String longMessage = "A".repeat(1000);
        InvalidAccountException exception = new InvalidAccountException(longMessage);
        
        assertEquals(longMessage, exception.getMessage());
    }

    @Test
    void testExceptionStackTrace() {
        InvalidAccountException exception = new InvalidAccountException("Test");
        
        assertNotNull(exception.getStackTrace());
        assertTrue(exception.getStackTrace().length > 0);
    }

    @Test
    void testExceptionCause() {
        InvalidAccountException exception = new InvalidAccountException("Test");
        
        assertNull(exception.getCause());
    }

    @Test
    void testExceptionEquality() {
        InvalidAccountException exception1 = new InvalidAccountException("Test");
        InvalidAccountException exception2 = new InvalidAccountException("Test");
        
        assertNotEquals(exception1, exception2);
    }

    @Test
    void testExceptionHashCode() {
        InvalidAccountException exception1 = new InvalidAccountException("Test");
        InvalidAccountException exception2 = new InvalidAccountException("Test");
        
        assertNotEquals(exception1.hashCode(), exception2.hashCode());
    }
}
```
