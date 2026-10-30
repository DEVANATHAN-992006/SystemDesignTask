
package com.rest.models;

import com.rest.models.enums.TransactionStatus;
import com.rest.models.enums.TransactionType;
import java.math.BigDecimal;

public class TransferTransaction extends Transaction {

    private int senderId;
    private int receiverId;

    public TransferTransaction(int transactionId, String referenceId,
                               BigDecimal amount, TransactionStatus status,
                               int senderId, int receiverId) {
        super(transactionId, referenceId, amount, status);
        this.senderId = senderId;
        this.receiverId = receiverId;
    }

    public int getSenderId() {
        return senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    @Override
    public TransactionType getType() {
        return TransactionType.TRANSFER;
    }
}
