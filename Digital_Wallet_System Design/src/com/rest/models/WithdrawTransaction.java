
package com.rest.models;

import com.rest.models.enums.TransactionStatus;
import com.rest.models.enums.TransactionType;
import java.math.BigDecimal;

public class WithdrawTransaction extends Transaction {

    private int userId;
    private int bankAccountId;

    public WithdrawTransaction(int transactionId, String referenceId,
                               BigDecimal amount, TransactionStatus status,
                               int userId, int bankAccountId) {
        super(transactionId, referenceId, amount, status);
        this.userId = userId;
        this.bankAccountId = bankAccountId;
    }

    public int getUserId() {
        return userId;
    }

    public int getBankAccountId() {
        return bankAccountId;
    }

    @Override
    public TransactionType getType() {
        return TransactionType.WITHDRAW;
    }
}
