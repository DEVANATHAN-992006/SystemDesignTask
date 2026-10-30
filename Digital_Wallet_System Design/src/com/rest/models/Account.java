
package com.rest.models;

import java.math.BigDecimal;

public abstract class Account {

    private int accountId;
    private BigDecimal balance;

    public Account(int accountId, BigDecimal balance) {
        this.accountId = accountId;
        this.balance = balance;
    }

    public int getAccountId() {
        return accountId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void credit(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Credit amount must be positive");
        }

        balance = balance.add(amount);
    }

    public boolean debit(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            return false;
        }

        if (balance.compareTo(amount) < 0) {
            return false;
        }

        balance = balance.subtract(amount);
        return true;
    }
}
