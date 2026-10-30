
package com.rest.service;

import com.rest.models.*;
import com.rest.models.enums.*;
import com.rest.repository.InMemoryStore;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class TransferService {

    private final InMemoryStore store;
    private final UserService userService;

    private static final BigDecimal MIN_TRANSFER =
            new BigDecimal("1.00");
    private static final BigDecimal MAX_TRANSFER =
            new BigDecimal("10000.00");
    private static final BigDecimal DAILY_LIMIT =
            new BigDecimal("25000.00");
    private static final BigDecimal BASIC_MONTHLY_LIMIT =
            new BigDecimal("20000.00");

    public TransferService(InMemoryStore store,
                           UserService userService) {
        this.store = store;
        this.userService = userService;
    }

    public TransferTransaction transfer(
            int senderId,
            String receiverPhone,
            BigDecimal amount,
            String pin) {

        User sender = userService.getUserById(senderId);
        userService.validateActiveUser(sender);

        // Validate the PIN before processing the transfer.
        userService.validatePin(sender, pin);

        if (!(sender instanceof Customer)) {
            throw new IllegalArgumentException(
                    "Only customers can send money.");
        }

        if (receiverPhone == null || receiverPhone.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Receiver phone number is required.");
        }

        User receiver = userService.getUserByPhone(receiverPhone.trim());
        userService.validateActiveUser(receiver);

        if (!(receiver instanceof Customer)) {
            throw new IllegalArgumentException(
                    "Receiver must be a customer.");
        }

        if (senderId == receiver.getUserId()) {
            throw new IllegalArgumentException(
                    "You cannot transfer money to yourself.");
        }

        if (amount == null || amount.compareTo(MIN_TRANSFER) < 0
                || amount.compareTo(MAX_TRANSFER) > 0) {
            throw new IllegalArgumentException(
                    "Transfer amount must be between ₹1 and ₹10,000.");
        }

        Wallet senderWallet = store.getWalletByUserId(senderId);
        Wallet receiverWallet =
                store.getWalletByUserId(receiver.getUserId());

        if (senderWallet == null || receiverWallet == null) {
            throw new IllegalStateException("Wallet not found.");
        }

        // Reject repeated transfers to the same receiver
        // for the same amount within one minute.
        if (isDuplicateTransfer(senderId, receiver.getUserId(), amount)) {
            throw new IllegalArgumentException(
                    "Duplicate transfer detected. Try again later.");
        }

        // Check the maximum amount sent today.
        if (getTodaySentTotal(senderId).add(amount)
                .compareTo(DAILY_LIMIT) > 0) {
            throw new IllegalStateException(
                    "Daily transfer limit of ₹25,000 exceeded.");
        }

        // Basic KYC has a ₹20,000 monthly outgoing transfer limit.
        if (sender.getKycStatus() == KycStatus.BASIC
                && getMonthlySentTotal(senderId).add(amount)
                .compareTo(BASIC_MONTHLY_LIMIT) > 0) {
            throw new IllegalStateException(
                    "Basic KYC monthly transfer limit exceeded.");
        }

        // Insufficient funds must create a FAILED transaction.
        if (senderWallet.getBalance().compareTo(amount) < 0) {
            return saveTransfer(senderId, receiver.getUserId(),
                    amount, TransactionStatus.FAILED);
        }

        // Both balance changes happen only after all checks pass.
        senderWallet.debit(amount);
        receiverWallet.credit(amount);

        return saveTransfer(senderId, receiver.getUserId(),
                amount, TransactionStatus.SUCCESS);
    }

    private TransferTransaction saveTransfer(
            int senderId, int receiverId, BigDecimal amount,
            TransactionStatus status) {

        TransferTransaction tx = new TransferTransaction(
                store.generateTransactionId(),
                "TRF-" + UUID.randomUUID(),
                amount, status, senderId, receiverId
        );

        store.addTransaction(tx);
        return tx;
    }

    private boolean isDuplicateTransfer(
            int senderId, int receiverId, BigDecimal amount) {

        for (Transaction tx : store.getAllTransactions()) {
            if (!(tx instanceof TransferTransaction)
                    || tx.getStatus() != TransactionStatus.SUCCESS) {
                continue;
            }

            TransferTransaction transfer = (TransferTransaction) tx;

            if (transfer.getSenderId() == senderId
                    && transfer.getReceiverId() == receiverId
                    && transfer.getAmount().compareTo(amount) == 0
                    && tx.getCreatedAt().isAfter(
                            java.time.LocalDateTime.now()
                                    .minusMinutes(1))) {
                return true;
            }
        }

        return false;
    }

    private BigDecimal getTodaySentTotal(int senderId) {
        BigDecimal total = BigDecimal.ZERO;
        LocalDate today = LocalDate.now();

        for (Transaction tx : store.getAllTransactions()) {
            if (!(tx instanceof TransferTransaction)
                    || tx.getStatus() != TransactionStatus.SUCCESS) {
                continue;
            }

            TransferTransaction transfer = (TransferTransaction) tx;

            if (transfer.getSenderId() == senderId
                    && tx.getCreatedAt().toLocalDate().equals(today)) {
                total = total.add(tx.getAmount());
            }
        }

        return total;
    }

    private BigDecimal getMonthlySentTotal(int senderId) {
        BigDecimal total = BigDecimal.ZERO;
        LocalDate today = LocalDate.now();

        for (Transaction tx : store.getAllTransactions()) {
            if (!(tx instanceof TransferTransaction)
                    || tx.getStatus() != TransactionStatus.SUCCESS) {
                continue;
            }

            TransferTransaction transfer = (TransferTransaction) tx;
            LocalDate transactionDate = tx.getCreatedAt().toLocalDate();

            if (transfer.getSenderId() == senderId
                    && transactionDate.getYear() == today.getYear()
                    && transactionDate.getMonth() == today.getMonth()) {
                total = total.add(tx.getAmount());
            }
        }

        return total;
    }
}
