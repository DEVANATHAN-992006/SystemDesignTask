
package com.rest.models;

import com.rest.models.enums.TransactionStatus;
import com.rest.models.enums.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public abstract class Transaction {

    private int transactionId;
    private String referenceId;
    private BigDecimal amount;
    private TransactionStatus status;
    private LocalDateTime createdAt;

    public Transaction(int transactionId, String referenceId,
                       BigDecimal amount, TransactionStatus status) {
        this.transactionId = transactionId;
        this.referenceId = referenceId;
        this.amount = amount;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    public int getTransactionId() {
        return transactionId;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public abstract TransactionType getType();

    @Override
    public String toString() {
        return "Transaction ID: " + transactionId
                + " | Reference: " + referenceId
                + " | Type: " + getType()
                + " | Amount: ₹" + amount
                + " | Status: " + status
                + " | Date: " + createdAt;
    }
}
