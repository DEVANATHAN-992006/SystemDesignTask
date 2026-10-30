
package com.rest.service;

import com.rest.models.*;
import com.rest.models.enums.*;
import com.rest.repository.InMemoryStore;

import java.time.LocalDateTime;

public class DisputeService {

    private final InMemoryStore store;

    public DisputeService(InMemoryStore store) {
        this.store = store;
    }

    // User raises a dispute within 24 hours
    public Dispute raiseDispute(
            int userId,
            String referenceId,
            String reason) {

        Transaction transaction = findTransaction(referenceId);

        if (!(transaction instanceof TransferTransaction)
                || transaction.getStatus() != TransactionStatus.SUCCESS) {
            throw new IllegalArgumentException(
                    "Only successful transfers can be disputed."
            );
        }

        TransferTransaction transfer =
                (TransferTransaction) transaction;

        if (transfer.getSenderId() != userId
                && transfer.getReceiverId() != userId) {
            throw new IllegalArgumentException(
                    "You are not involved in this transfer."
            );
        }

        if (transaction.getCreatedAt().isBefore(
                LocalDateTime.now().minusHours(24))) {
            throw new IllegalArgumentException(
                    "Dispute must be raised within 24 hours."
            );
        }

        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Please provide a dispute reason."
            );
        }

        Dispute dispute = new Dispute(
                store.generateDisputeId(),
                referenceId,
                userId,
                reason.trim()
        );

        store.addDispute(dispute);
        return dispute;
    }

    // Only an admin can approve or reject a dispute
    public Reversal reviewDispute(
            int adminId,
            int disputeId,
            boolean approve) {

        User admin = store.getUserById(adminId);

        if (!(admin instanceof Admin)
                || admin.getRole() != UserRole.ADMIN) {
            throw new IllegalStateException(
                    "Only an admin can review disputes."
            );
        }

        Dispute dispute = findDispute(disputeId);

        if (dispute.getStatus() != DisputeStatus.RAISED
                && dispute.getStatus() != DisputeStatus.UNDER_REVIEW) {
            throw new IllegalStateException(
                    "This dispute has already been reviewed."
            );
        }

        if (!approve) {
            dispute.review(DisputeStatus.REJECTED, adminId);
            return null;
        }

        Transaction transaction =
                findTransaction(dispute.getTransactionReferenceId());

        if (!(transaction instanceof TransferTransaction)
                || transaction.getStatus() != TransactionStatus.SUCCESS) {
            throw new IllegalStateException(
                    "Transfer cannot be reversed."
            );
        }

        TransferTransaction transfer =
                (TransferTransaction) transaction;

        Reversal reversal = new Reversal(
                store.generateReversalId(),
                transfer.getReferenceId(),
                transfer.getAmount()
        );

        store.addReversal(reversal);
        dispute.review(DisputeStatus.APPROVED, adminId);

        processReversal(reversal, transfer);

        return reversal;
    }

    // Process reversal if receiver has enough money
    public void processReversal(
            Reversal reversal,
            TransferTransaction transfer) {

        if (reversal.getStatus() != ReversalStatus.PENDING) {
            return;
        }

        Wallet senderWallet =
                store.getWalletByUserId(transfer.getSenderId());

        Wallet receiverWallet =
                store.getWalletByUserId(transfer.getReceiverId());

        if (senderWallet == null || receiverWallet == null) {
            throw new IllegalStateException("Wallet not found.");
        }

        if (receiverWallet.getBalance().compareTo(
                reversal.getAmount()) < 0) {
            return; // Keep reversal pending until funds are available
        }

        receiverWallet.debit(reversal.getAmount());
        senderWallet.credit(reversal.getAmount());

        reversal.setStatus(ReversalStatus.COMPLETED);
        transfer.setStatus(TransactionStatus.REVERSED);
    }

    private Transaction findTransaction(String referenceId) {
        for (Transaction transaction : store.getAllTransactions()) {
            if (transaction.getReferenceId().equals(referenceId)) {
                return transaction;
            }
        }

        throw new IllegalArgumentException("Transaction not found.");
    }

    private Dispute findDispute(int disputeId) {
        for (Dispute dispute : store.getAllDisputes()) {
            if (dispute.getDisputeId() == disputeId) {
                return dispute;
            }
        }

        throw new IllegalArgumentException("Dispute not found.");
    }
}
