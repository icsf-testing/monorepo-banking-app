Here's a JUnit 5 test class for the ServiceConfig class:

```java
package com.banking.api.config;

import com.banking.account.service.AccountService;
import com.banking.transaction.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

class ServiceConfigTest {

    @Test
    void testAccountServiceBeanCreation() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ServiceConfig.class);
        AccountService accountService = context.getBean(AccountService.class);
        assertNotNull(accountService);
        context.close();
    }

    @Test
    void testTransactionServiceBeanCreation() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ServiceConfig.class);
        TransactionService transactionService = context.getBean(TransactionService.class);
        assertNotNull(transactionService);
        context.close();
    }

    @Test
    void testTransactionServiceDependencyInjection() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ServiceConfig.class);
        TransactionService transactionService = context.getBean(TransactionService.class);
        AccountService accountService = context.getBean(AccountService.class);
        
        assertNotNull(transactionService);
        assertNotNull(accountService);
        
        // Assuming TransactionService has a method to get the injected AccountService
        // If not, this part of the test might need to be adjusted or removed
        assertEquals(accountService, transactionService.getAccountService());
        
        context.close();
    }

    @Test
    void testMultipleContextCreations() {
        AnnotationConfigApplicationContext context1 = new AnnotationConfigApplicationContext(ServiceConfig.class);
        AnnotationConfigApplicationContext context2 = new AnnotationConfigApplicationContext(ServiceConfig.class);
        
        AccountService accountService1 = context1.getBean(AccountService.class);
        AccountService accountService2 = context2.getBean(AccountService.class);
        
        assertNotNull(accountService1);
        assertNotNull(accountService2);
        assertNotSame(accountService1, accountService2);
        
        context1.close();
        context2.close();
    }

    @Test
    void testBeanScopes() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ServiceConfig.class);
        
        AccountService accountService1 = context.getBean(AccountService.class);
        AccountService accountService2 = context.getBean(AccountService.class);
        
        assertSame(accountService1, accountService2, "AccountService should be a singleton");
        
        TransactionService transactionService1 = context.getBean(TransactionService.class);
        TransactionService transactionService2 = context.getBean(TransactionService.class);
        
        assertSame(transactionService1, transactionService2, "TransactionService should be a singleton");
        
        context.close();
    }
}
```
