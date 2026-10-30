
package com.rest.service;

import com.rest.models.*;
import com.rest.models.enums.*;
import com.rest.repository.InMemoryStore;

import java.util.regex.Pattern;

public class UserService {

    private final InMemoryStore store;

    public UserService(InMemoryStore store) {
        this.store = store;
    }

    public Customer registerCustomer(
            String name, String phone, String email, String pin) {

        validateDetails(name, phone, email, pin);

        if (store.getUserByPhone(phone) != null) {
            throw new IllegalArgumentException(
                    "Phone number already registered.");
        }

        Customer customer = new Customer(
                store.generateUserId(),
                name.trim(), phone, email.trim(), pin,
                KycStatus.BASIC, UserStatus.ACTIVE
        );

        store.addUser(customer);
        store.addWallet(new Wallet(
                store.generateWalletId(), customer.getUserId()));

        return customer;
    }

    public User getUserByPhone(String phone) {
        User user = store.getUserByPhone(phone);

        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }

        return user;
    }

    public User getUserById(int userId) {
        User user = store.getUserById(userId);

        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }

        return user;
    }

    public void validateActiveUser(User user) {
        if (user == null || user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalStateException(
                    "User is unavailable or blocked.");
        }
    }

    public void validatePin(User user, String enteredPin) {
        validateActiveUser(user);

        if (user.isTransactionLocked()) {
            throw new IllegalStateException(
                    "Transactions are locked for 24 hours.");
        }

        if (user.verifyPin(enteredPin)) {
            user.resetWrongPinAttempts();
            return;
        }

        user.incrementWrongPinAttempts();

        if (user.getWrongPinAttempts() >= 3) {
            user.lockTransactions();
            throw new IllegalStateException(
                    "Three incorrect attempts. Transactions locked for 24 hours.");
        }

        throw new IllegalArgumentException(
                "Incorrect PIN. Attempts remaining: "
                + (3 - user.getWrongPinAttempts()));
    }

    public void upgradeKyc(int userId) {
        User user = getUserById(userId);

        if (!(user instanceof Customer)) {
            throw new IllegalArgumentException(
                    "Only customers can upgrade KYC.");
        }

        user.setKycStatus(KycStatus.FULL);
    }

    public void setUserStatus(int userId, UserStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null.");
        }

        getUserById(userId).setStatus(status);
    }

    private void validateDetails(
            String name, String phone, String email, String pin) {

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required.");
        }

        if (phone == null || !phone.matches("\\d{10}")) {
            throw new IllegalArgumentException(
                    "Phone must contain exactly 10 digits.");
        }

        if (email == null || !Pattern.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
                email.trim())) {
            throw new IllegalArgumentException("Invalid email address.");
        }

        if (pin == null || !pin.matches("\\d{4}")) {
            throw new IllegalArgumentException(
                    "PIN must contain exactly 4 digits.");
        }
    }
}
