
package com.rest.models;

import com.rest.models.enums.KycStatus;
import java.math.BigDecimal;

public class Wallet extends Account {

    private int userId;

    public Wallet(int walletId, int userId) {
        super(walletId, BigDecimal.ZERO);
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    public BigDecimal getBalanceLimit(KycStatus kycStatus) {
        if (kycStatus == KycStatus.BASIC) {
            return new BigDecimal("10000.00");
        }

        return new BigDecimal("100000.00");
    }

    public boolean canHold(BigDecimal amount, KycStatus kycStatus) {
        if (amount == null || amount.signum() < 0) {
            return false;
        }

        BigDecimal newBalance = getBalance().add(amount);

        return newBalance.compareTo(getBalanceLimit(kycStatus)) <= 0;
    }
}
