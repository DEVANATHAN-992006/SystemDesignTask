
package com.rest.models;

import java.math.BigDecimal;

public class BankAccount extends Account {

    private int userId;
    private int bankId;
    private String accountNumber;
    private boolean primary;
    private boolean active;

    public BankAccount(int accountId, int userId, int bankId,
                       String accountNumber, BigDecimal balance,
                       boolean primary) {

        super(accountId, balance);
        this.userId = userId;
        this.bankId = bankId;
        this.accountNumber = accountNumber;
        this.primary = primary;
        this.active = true;
    }

    public int getUserId() {
        return userId;
    }

    public int getBankId() {
        return bankId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public boolean isPrimary() {
        return primary;
    }

    public boolean isActive() {
        return active;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "Bank Account: " + accountNumber
                + " | Balance: ₹" + getBalance()
                + " | Primary: " + primary;
    }
}
