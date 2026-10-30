
package com.rest.models;

import com.rest.models.enums.ReversalStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Reversal {

    private int reversalId;
    private String originalReferenceId;
    private BigDecimal amount;
    private ReversalStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Reversal(int reversalId, String originalReferenceId,
                    BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Reversal amount must be positive");
        }

        this.reversalId = reversalId;
        this.originalReferenceId = originalReferenceId;
        this.amount = amount;
        this.status = ReversalStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public int getReversalId() {
        return reversalId;
    }

    public String getOriginalReferenceId() {
        return originalReferenceId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public ReversalStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setStatus(ReversalStatus status) {
        if (status == null) {
            throw new IllegalArgumentException(
                    "Reversal status cannot be null");
        }

        if (this.status != ReversalStatus.PENDING) {
            throw new IllegalStateException(
                    "Only a pending reversal can change status");
        }

        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
}
