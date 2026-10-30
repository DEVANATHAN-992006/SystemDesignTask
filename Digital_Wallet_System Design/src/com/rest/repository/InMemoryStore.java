
package com.rest.repository;

import com.rest.models.*;
import java.util.*;

public class InMemoryStore {

    // Main storage collections
    private final Map<Integer, User> users = new HashMap<>();
    private final Map<String, Integer> userIdsByPhone = new HashMap<>();

    // Wallets are indexed by their owner's user ID
    private final Map<Integer, Wallet> wallets = new HashMap<>();

    private final Map<Integer, Bank> banks = new HashMap<>();

    private final Map<Integer, BankAccount> bankAccounts = new HashMap<>();
    private final Map<String, Integer> bankAccountIdsByNumber =
            new HashMap<>();

    private final List<Transaction> transactions = new ArrayList<>();
    private final List<Dispute> disputes = new ArrayList<>();
    private final List<Reversal> reversals = new ArrayList<>();
    private final List<CashbackRecord> cashbackRecords = new ArrayList<>();

    // ID counters
    private int nextUserId = 1;
    private int nextWalletId = 1;
    private int nextBankId = 1;
    private int nextBankAccountId = 1;
    private int nextTransactionId = 1;
    private int nextDisputeId = 1;
    private int nextReversalId = 1;
    private int nextCashbackId = 1;

    // ---------- ID generators ----------

    public int generateUserId() {
        return nextUserId++;
    }

    public int generateWalletId() {
        return nextWalletId++;
    }

    public int generateBankId() {
        return nextBankId++;
    }

    public int generateBankAccountId() {
        return nextBankAccountId++;
    }

    public int generateTransactionId() {
        return nextTransactionId++;
    }

    public int generateDisputeId() {
        return nextDisputeId++;
    }

    public int generateReversalId() {
        return nextReversalId++;
    }

    public int generateCashbackId() {
        return nextCashbackId++;
    }

    // ---------- User operations ----------

    public void addUser(User user) {
        Objects.requireNonNull(user, "User cannot be null");

        if (users.containsKey(user.getUserId())) {
            throw new IllegalArgumentException("User ID already exists");
        }

        if (userIdsByPhone.containsKey(user.getPhone())) {
            throw new IllegalArgumentException("Phone number already exists");
        }

        users.put(user.getUserId(), user);
        userIdsByPhone.put(user.getPhone(), user.getUserId());
    }

    public User getUserById(int userId) {
        return users.get(userId);
    }

    public User getUserByPhone(String phone) {
        Integer userId = userIdsByPhone.get(phone);
        return userId == null ? null : users.get(userId);
    }

    public Collection<User> getAllUsers() {
        return Collections.unmodifiableCollection(users.values());
    }

    // ---------- Wallet operations ----------

    public void addWallet(Wallet wallet) {
        Objects.requireNonNull(wallet, "Wallet cannot be null");

        if (wallets.containsKey(wallet.getUserId())) {
            throw new IllegalArgumentException(
                    "User already has a wallet");
        }

        wallets.put(wallet.getUserId(), wallet);
    }

    public Wallet getWalletByUserId(int userId) {
        return wallets.get(userId);
    }

    public Collection<Wallet> getAllWallets() {
        return Collections.unmodifiableCollection(wallets.values());
    }

    // ---------- Bank operations ----------

    public void addBank(Bank bank) {
        Objects.requireNonNull(bank, "Bank cannot be null");

        if (banks.containsKey(bank.getBankId())) {
            throw new IllegalArgumentException("Bank ID already exists");
        }

        banks.put(bank.getBankId(), bank);
    }

    public Bank getBankById(int bankId) {
        return banks.get(bankId);
    }

    public Collection<Bank> getAllBanks() {
        return Collections.unmodifiableCollection(banks.values());
    }

    // ---------- Bank account operations ----------

    public void addBankAccount(BankAccount account) {
        Objects.requireNonNull(account, "Bank account cannot be null");

        if (bankAccounts.containsKey(account.getAccountId())) {
            throw new IllegalArgumentException(
                    "Bank account ID already exists");
        }

        if (bankAccountIdsByNumber.containsKey(account.getAccountNumber())) {
            throw new IllegalArgumentException(
                    "Bank account number already exists");
        }

        bankAccounts.put(account.getAccountId(), account);
        bankAccountIdsByNumber.put(
                account.getAccountNumber(), account.getAccountId());
    }

    public BankAccount getBankAccountById(int accountId) {
        return bankAccounts.get(accountId);
    }

    public BankAccount getBankAccountByNumber(String accountNumber) {
        Integer accountId = bankAccountIdsByNumber.get(accountNumber);
        return accountId == null ? null : bankAccounts.get(accountId);
    }

    public List<BankAccount> getBankAccountsByUserId(int userId) {
        List<BankAccount> result = new ArrayList<>();

        for (BankAccount account : bankAccounts.values()) {
            if (account.getUserId() == userId && account.isActive()) {
                result.add(account);
            }
        }

        return result;
    }

    public Collection<BankAccount> getAllBankAccounts() {
        return Collections.unmodifiableCollection(bankAccounts.values());
    }

    // ---------- Transaction operations ----------

    public void addTransaction(Transaction transaction) {
        Objects.requireNonNull(transaction, "Transaction cannot be null");
        transactions.add(transaction);
    }

    public List<Transaction> getAllTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    // ---------- Dispute operations ----------

    public void addDispute(Dispute dispute) {
        Objects.requireNonNull(dispute, "Dispute cannot be null");
        disputes.add(dispute);
    }

    public List<Dispute> getAllDisputes() {
        return Collections.unmodifiableList(disputes);
    }

    // ---------- Reversal operations ----------

    public void addReversal(Reversal reversal) {
        Objects.requireNonNull(reversal, "Reversal cannot be null");
        reversals.add(reversal);
    }

    public List<Reversal> getAllReversals() {
        return Collections.unmodifiableList(reversals);
    }

    // ---------- Cashback operations ----------

    public void addCashbackRecord(CashbackRecord record) {
        Objects.requireNonNull(record, "Cashback record cannot be null");
        cashbackRecords.add(record);
    }

    public List<CashbackRecord> getAllCashbackRecords() {
        return Collections.unmodifiableList(cashbackRecords);
    }
}
