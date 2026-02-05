package com.banking.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest
class BankingApiApplicationTest {

    @MockBean
    private SpringApplication springApplication;

    @Test
    void contextLoads() {
        // This test ensures that the Spring context can be loaded without errors
    }

    @Test
    void mainMethodShouldCallSpringApplicationRun() {
        // Arrange
        String[] args = {};

        // Act
        BankingApiApplication.main(args);

        // Assert
        verify(springApplication).run(eq(BankingApiApplication.class), any(String[].class));
    }

    @Test
    void mainMethodShouldHandleNonEmptyArgs() {
        // Arrange
        String[] args = {"arg1", "arg2"};

        // Act
        BankingApiApplication.main(args);

        // Assert
        verify(springApplication).run(eq(BankingApiApplication.class), eq(args));
    }

    @Test
    void mainMethodShouldHandleNullArgs() {
        // Arrange
        String[] args = null;

        // Act
        BankingApiApplication.main(args);

        // Assert
        verify(springApplication).run(eq(BankingApiApplication.class), any(String[].class));
    }
}
