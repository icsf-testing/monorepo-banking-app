================================================================================
FIXED CODE FOR: banking-api/src/main/java/com/banking/api/dto/TransactionRequest.java
================================================================================
package com.banking.api.dto;

import javax.validation.constraints.*;
import org.hibernate.validator.constraints.Length;
import java.util.regex.Pattern;
import org.springframework.security.access.prepost.PreAuthorize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.banking.api.service.TokenizationService;
import com.banking.api.service.KeyManagementService;

public class TransactionRequest {
    private static final Logger logger = LoggerFactory.getLogger(TransactionRequest.class);

    private final TokenizationService tokenizationService;
    private final KeyManagementService keyManagementService;

    @NotBlank(message = "Account ID token is required")
    @Pattern(regexp = "^[A-Za-z0-9-_]{22}$", message = "Invalid account ID token format")
    private String accountIdToken;

    @NotBlank(message = "From Account ID token is required")
    @Pattern(regexp = "^[A-Za-z0-9-_]{22}$", message = "Invalid from account ID token format")
    private String fromAccountIdToken;

    @NotBlank(message = "To Account ID token is required")
    @Pattern(regexp = "^[A-Za-z0-9-_]{22}$", message = "Invalid to account ID token format")
    private String toAccountIdToken;

    @Positive(message = "Amount must be positive")
    private double amount;

    @NotBlank(message = "Currency is required")
    @Length(min = 3, max = 3, message = "Currency must be a 3-letter code")
    private String currency;

    @NotBlank(message = "Description is required")
    @Length(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    public TransactionRequest(TokenizationService tokenizationService, KeyManagementService keyManagementService) {
        this.tokenizationService = tokenizationService;
        this.keyManagementService = keyManagementService;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or @securityService.hasAccountAccess(authentication, #accountIdToken)")
    public String getAccountIdToken() {
        return accountIdToken;
    }

    public void setAccountIdToken(String accountIdToken) {
        this.accountIdToken = sanitizeInput(accountIdToken);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or @securityService.hasAccountAccess(authentication, #fromAccountIdToken)")
    public String getFromAccountIdToken() {
        return fromAccountIdToken;
    }

    public void setFromAccountIdToken(String fromAccountIdToken) {
        this.fromAccountIdToken = sanitizeInput(fromAccountIdToken);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or @securityService.hasAccountAccess(authentication, #toAccountIdToken)")
    public String getToAccountIdToken() {
        return toAccountIdToken;
    }

    public void setToAccountIdToken(String toAccountIdToken) {
        this.toAccountIdToken = sanitizeInput(toAccountIdToken);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or @securityService.hasAccountAccess(authentication, #accountIdToken)")
    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = sanitizeInput(currency);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = sanitizeInput(description);
    }

    private String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        return Pattern.compile("[<>&'\"]").matcher(input).replaceAll("");
    }

    public String detokenizeAccountId(String accountIdToken) {
        try {
            return tokenizationService.detokenize(accountIdToken);
        } catch (Exception e) {
            logger.error("Error detokenizing account ID", e);
            throw new RuntimeException("Error processing account ID", e);
        }
    }
}