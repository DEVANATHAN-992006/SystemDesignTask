
package com.rest.service;

import com.rest.models.*;
import com.rest.models.enums.*;
import com.rest.repository.InMemoryStore;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AccountService {

    private final InMemoryStore store;

    public AccountService(InMemoryStore store) {
        this.store = store;
    }

    // ---------- Bank accounts ----------

    public BankAccount linkBankAccount(
            int userId, int bankId, String accountNumber,
            BigDecimal initialBalance) {

        User user = getCustomer(userId);

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalStateException("User is blocked.");
        }

        if (store.getBankById(bankId) == null) {
            throw new IllegalArgumentException("Bank not found.");
        }

        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Account number is required.");
        }

        if (initialBalance == null || initialBalance.signum() < 0) {
            throw new IllegalArgumentException(
                    "Invalid initial bank balance.");
        }

        List<BankAccount> accounts =
                store.getBankAccountsByUserId(userId);

        if (accounts.size() >= 3) {
            throw new IllegalStateException(
                    "Maximum 3 bank accounts allowed.");
        }

        if (store.getBankAccountByNumber(accountNumber.trim()) != null) {
            throw new IllegalArgumentException(
                    "Account number already linked.");
        }

        BankAccount account = new BankAccount(
                store.generateBankAccountId(),
                userId, bankId, accountNumber.trim(),
                initialBalance, accounts.isEmpty()
        );

        store.addBankAccount(account);
        return account;
    }

    public List<BankAccount> getLinkedBankAccounts(int userId) {
        getCustomer(userId);
        return new ArrayList<>(
                store.getBankAccountsByUserId(userId));
    }

    public void setPrimaryBankAccount(int userId, int accountId) {
        BankAccount selected = getOwnedAccount(userId, accountId);

        for (BankAccount account :
                store.getBankAccountsByUserId(userId)) {
            account.setPrimary(
                    account.getAccountId() == selected.getAccountId());
        }
    }

    public void unlinkBankAccount(int userId, int accountId) {
        BankAccount account = getOwnedAccount(userId, accountId);

        for (Transaction tx : store.getAllTransactions()) {
            if (tx.getStatus() != TransactionStatus.PENDING) {
                continue;
            }

            if (tx instanceof AddMoneyTransaction
                    && ((AddMoneyTransaction) tx).getBankAccountId()
                    == accountId) {
                throw new IllegalStateException(
                        "Account has a pending transaction.");
            }

            if (tx instanceof WithdrawTransaction
                    && ((WithdrawTransaction) tx).getBankAccountId()
                    == accountId) {
                throw new IllegalStateException(
                        "Account has a pending transaction.");
            }
        }

        boolean wasPrimary = account.isPrimary();
        account.setActive(false);
        account.setPrimary(false);

        if (wasPrimary) {
            List<BankAccount> remaining =
                    store.getBankAccountsByUserId(userId);

            if (!remaining.isEmpty()) {
                remaining.get(0).setPrimary(true);
            }
        }
    }

    // ---------- Add money ----------

    public AddMoneyTransaction addMoney(
            int userId, int accountId, BigDecimal amount) {

        User user = getCustomer(userId);
        validateActiveUser(user);

        Wallet wallet = getWallet(userId);
        BankAccount bankAccount = getOwnedAccount(userId, accountId);

        validateAmount(amount);

        if (amount.compareTo(new BigDecimal("10")) < 0
                || amount.compareTo(new BigDecimal("50000")) > 0) {
            throw new IllegalArgumentException(
                    "Add money must be between ₹10 and ₹50,000.");
        }

        if (!wallet.canHold(amount, user.getKycStatus())) {
            return saveAddMoney(userId, accountId, amount,
                    TransactionStatus.FAILED);
        }

        if (bankAccount.getBalance().compareTo(amount) < 0) {
            return saveAddMoney(userId, accountId, amount,
                    TransactionStatus.FAILED);
        }

        // Validate first, then update both balances.
        bankAccount.debit(amount);
        wallet.credit(amount);

        return saveAddMoney(userId, accountId, amount,
                TransactionStatus.SUCCESS);
    }

    // ---------- Withdraw money ----------

    public WithdrawTransaction withdrawMoney(
            int userId, int accountId, BigDecimal amount) {

        User user = getCustomer(userId);
        validateActiveUser(user);

        Wallet wallet = getWallet(userId);
        BankAccount bankAccount = getOwnedAccount(userId, accountId);

        validateAmount(amount);

        if (wallet.getBalance().compareTo(amount) < 0) {
            return saveWithdrawal(userId, accountId, amount,
                    TransactionStatus.FAILED);
        }

        wallet.debit(amount);
        bankAccount.credit(amount);

        return saveWithdrawal(userId, accountId, amount,
                TransactionStatus.SUCCESS);
    }

    // ---------- Helpers ----------

    private User getCustomer(int userId) {
        User user = store.getUserById(userId);

        if (!(user instanceof Customer)) {
            throw new IllegalArgumentException("Customer not found.");
        }

        return user;
    }

    private void validateActiveUser(User user) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Blocked users cannot transact.");
        }
    }

    private Wallet getWallet(int userId) {
        Wallet wallet = store.getWalletByUserId(userId);

        if (wallet == null) {
            throw new IllegalStateException("Wallet not found.");
        }

        return wallet;
    }

    private BankAccount getOwnedAccount(int userId, int accountId) {
        BankAccount account = store.getBankAccountById(accountId);

        if (account == null || !account.isActive()
                || account.getUserId() != userId) {
            throw new IllegalArgumentException(
                    "Linked bank account not found.");
        }

        return account;
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be positive.");
        }
    }

    private AddMoneyTransaction saveAddMoney(
            int userId, int accountId, BigDecimal amount,
            TransactionStatus status) {

        AddMoneyTransaction tx = new AddMoneyTransaction(
                store.generateTransactionId(),
                "ADD-" + UUID.randomUUID(),
                amount, status, userId, accountId
        );

        store.addTransaction(tx);
        return tx;
    }

    private WithdrawTransaction saveWithdrawal(
            int userId, int accountId, BigDecimal amount,
            TransactionStatus status) {

        WithdrawTransaction tx = new WithdrawTransaction(
                store.generateTransactionId(),
                "WDR-" + UUID.randomUUID(),
                amount, status, userId, accountId
        );

        store.addTransaction(tx);
        return tx;
    }
}
