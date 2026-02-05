Here's a JUnit 5 test class for the InsufficientFundsException:

```java
package com.banking.core.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InsufficientFundsExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String errorMessage = "Insufficient funds for withdrawal";
        InsufficientFundsException exception = new InsufficientFundsException(errorMessage);
        
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void testExceptionIsRuntimeException() {
        InsufficientFundsException exception = new InsufficientFundsException("Test");
        
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void testConstructorWithNullMessage() {
        InsufficientFundsException exception = new InsufficientFundsException(null);
        
        assertNull(exception.getMessage());
    }

    @Test
    void testConstructorWithEmptyMessage() {
        InsufficientFundsException exception = new InsufficientFundsException("");
        
        assertEquals("", exception.getMessage());
    }

    @Test
    void testExceptionWithLongMessage() {
        String longMessage = "A".repeat(1000);
        InsufficientFundsException exception = new InsufficientFundsException(longMessage);
        
        assertEquals(longMessage, exception.getMessage());
    }

    @Test
    void testExceptionStackTrace() {
        InsufficientFundsException exception = new InsufficientFundsException("Test");
        
        assertNotNull(exception.getStackTrace());
        assertTrue(exception.getStackTrace().length > 0);
    }
}
```
