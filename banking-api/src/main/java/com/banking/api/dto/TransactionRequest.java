================================================================================
FIXED CODE FOR: banking-api/src/main/java/com/banking/api/dto/TransactionRequest.java
================================================================================
package com.banking.api.dto;

import javax.validation.constraints.*;
import org.hibernate.validator.constraints.Length;
import java.util.regex.Pattern;

public class TransactionRequest {
    @NotBlank(message = "Account ID is required")
    @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "Invalid account ID format")
    private String accountId;

    @NotBlank(message = "From Account ID is required")
    @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "Invalid from account ID format")
    private String fromAccountId;

    @NotBlank(message = "To Account ID is required")
    @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "Invalid to account ID format")
    private String toAccountId;

    @Positive(message = "Amount must be positive")
    private double amount;

    @NotBlank(message = "Currency is required")
    @Length(min = 3, max = 3, message = "Currency must be a 3-letter code")
    private String currency;

    @NotBlank(message = "Description is required")
    @Length(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = sanitizeInput(accountId);
    }

    public String getFromAccountId() {
        return fromAccountId;
    }

    public void setFromAccountId(String fromAccountId) {
        this.fromAccountId = sanitizeInput(fromAccountId);
    }

    public String getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(String toAccountId) {
        this.toAccountId = sanitizeInput(toAccountId);
    }

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
}