
package com.rest.models;

import com.rest.models.enums.CashbackStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CashbackRecord {

    private int cashbackId;
    private int userId;
    private String originalReferenceId;
    private BigDecimal amount;
    private CashbackStatus status;
    private LocalDateTime createdAt;

    public CashbackRecord(int cashbackId, int userId,
                          String originalReferenceId,
                          BigDecimal amount, CashbackStatus status) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Cashback amount must be positive");
        }

        this.cashbackId = cashbackId;
        this.userId = userId;
        this.originalReferenceId = originalReferenceId;
        this.amount = amount;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    public int getCashbackId() {
        return cashbackId;
    }

    public int getUserId() {
        return userId;
    }

    public String getOriginalReferenceId() {
        return originalReferenceId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public CashbackStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setStatus(CashbackStatus status) {
        if (status == null) {
            throw new IllegalArgumentException(
                    "Cashback status cannot be null");
        }

        this.status = status;
    }
}
