
package com.rest.service;

import com.rest.models.*;
import com.rest.models.enums.TransactionStatus;
import com.rest.models.enums.TransactionType;
import com.rest.repository.InMemoryStore;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TransactionService {

    private final InMemoryStore store;

    public TransactionService(InMemoryStore store) {
        this.store = store;
    }

    // Get all transactions belonging to a user
    public List<Transaction> getHistory(int userId) {
        List<Transaction> history = new ArrayList<>();

        for (Transaction tx : store.getAllTransactions()) {
            if (belongsToUser(tx, userId)) {
                history.add(tx);
            }
        }

        history.sort(
            Comparator.comparing(Transaction::getCreatedAt).reversed()
        );

        return history;
    }

    // Filter transactions by type, status and date
    public List<Transaction> filterHistory(
            int userId,
            TransactionType type,
            TransactionStatus status,
            LocalDate startDate,
            LocalDate endDate) {

        if (startDate != null && endDate != null
                && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                "Start date cannot be after end date."
            );
        }

        List<Transaction> result = new ArrayList<>();

        for (Transaction tx : getHistory(userId)) {

            if (type != null && tx.getType() != type) {
                continue;
            }

            if (status != null && tx.getStatus() != status) {
                continue;
            }

            LocalDate txDate = tx.getCreatedAt().toLocalDate();

            if (startDate != null && txDate.isBefore(startDate)) {
                continue;
            }

            if (endDate != null && txDate.isAfter(endDate)) {
                continue;
            }

            result.add(tx);
        }

        return result;
    }

    // Check whether the user sent, received or made the transaction
    private boolean belongsToUser(Transaction tx, int userId) {

        if (tx instanceof AddMoneyTransaction) {
            return ((AddMoneyTransaction) tx).getUserId() == userId;
        }

        if (tx instanceof WithdrawTransaction) {
            return ((WithdrawTransaction) tx).getUserId() == userId;
        }

        if (tx instanceof TransferTransaction) {
            TransferTransaction transfer = (TransferTransaction) tx;

            return transfer.getSenderId() == userId
                    || transfer.getReceiverId() == userId;
        }

        if (tx instanceof CashbackTransaction) {
            return ((CashbackTransaction) tx).getUserId() == userId;
        }

        if (tx instanceof RefundTransaction) {
            return ((RefundTransaction) tx).getUserId() == userId;
        }

        return false;
    }
}
