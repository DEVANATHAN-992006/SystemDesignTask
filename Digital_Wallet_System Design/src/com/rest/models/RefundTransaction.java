
package com.rest.models;

import com.rest.models.enums.TransactionStatus;
import com.rest.models.enums.TransactionType;
import java.math.BigDecimal;

public class RefundTransaction extends Transaction {

    private int userId;
    private String originalReferenceId;

    public RefundTransaction(int transactionId, String referenceId,
                             BigDecimal amount, TransactionStatus status,
                             int userId, String originalReferenceId) {
        super(transactionId, referenceId, amount, status);
        this.userId = userId;
        this.originalReferenceId = originalReferenceId;
    }

    public int getUserId() {
        return userId;
    }

    public String getOriginalReferenceId() {
        return originalReferenceId;
    }

    @Override
    public TransactionType getType() {
        return TransactionType.REFUND;
    }
}
