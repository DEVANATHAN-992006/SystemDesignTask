
package com.rest.service;

import com.rest.models.*;
import com.rest.models.enums.*;
import com.rest.repository.InMemoryStore;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

public class RewardService {

    private final InMemoryStore store;

    private static final BigDecimal MIN_TRANSFER =
            new BigDecimal("500.00");

    private static final BigDecimal CASHBACK_RATE =
            new BigDecimal("0.01");

    private static final BigDecimal MAX_CASHBACK =
            new BigDecimal("20.00");

    private static final BigDecimal MONTHLY_LIMIT =
            new BigDecimal("100.00");

    public RewardService(InMemoryStore store) {
        this.store = store;
    }

    // Give cashback after a successful transfer
    public CashbackRecord giveCashback(
            TransferTransaction transaction) {

        if (transaction == null
                || transaction.getStatus() != TransactionStatus.SUCCESS) {
            return null;
        }

        BigDecimal amount = transaction.getAmount();

        if (amount.compareTo(MIN_TRANSFER) < 0) {
            return null;
        }

        int userId = transaction.getSenderId();

        User user = store.getUserById(userId);
        Wallet wallet = store.getWalletByUserId(userId);

        if (user == null || wallet == null) {
            return null;
        }

        BigDecimal cashback = amount.multiply(CASHBACK_RATE);

        if (cashback.compareTo(MAX_CASHBACK) > 0) {
            cashback = MAX_CASHBACK;
        }

        // Check monthly cashback limit
        YearMonth currentMonth = YearMonth.now();
        BigDecimal monthlyCashback = BigDecimal.ZERO;

        for (CashbackRecord record : store.getAllCashbackRecords()) {
            if (record.getUserId() == userId
                    && record.getStatus() == CashbackStatus.CREDITED
                    && YearMonth.from(record.getCreatedAt())
                            .equals(currentMonth)) {

                monthlyCashback = monthlyCashback.add(record.getAmount());
            }
        }

        BigDecimal remainingLimit =
                MONTHLY_LIMIT.subtract(monthlyCashback);

        if (cashback.compareTo(remainingLimit) > 0) {
            cashback = remainingLimit;
        }

        // Skip cashback if wallet limit would be exceeded
        if (cashback.signum() <= 0
                || !wallet.canHold(cashback, user.getKycStatus())) {
            return null;
        }

        wallet.credit(cashback);

        CashbackTransaction cashbackTransaction =
                new CashbackTransaction(
                        store.generateTransactionId(),
                        "CB-" + UUID.randomUUID(),
                        cashback,
                        TransactionStatus.SUCCESS,
                        userId,
                        transaction.getReferenceId());

        store.addTransaction(cashbackTransaction);

        CashbackRecord record = new CashbackRecord(
                store.generateCashbackId(),
                userId,
                transaction.getReferenceId(),
                cashback,
                CashbackStatus.CREDITED);

        store.addCashbackRecord(record);

        return record;
    }
}
