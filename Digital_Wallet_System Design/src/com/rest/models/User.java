
package com.rest.models;

import com.rest.models.enums.KycStatus;
import com.rest.models.enums.UserRole;
import com.rest.models.enums.UserStatus;

import java.time.LocalDateTime;

public abstract class User {

    private int userId;
    private String name;
    private String phone;
    private String email;
    private String pin;
    private KycStatus kycStatus;
    private UserStatus status;
    private UserRole role;

    private int wrongPinAttempts;
    private LocalDateTime transactionLockedUntil;

    public User(int userId, String name, String phone,
                String email, String pin, KycStatus kycStatus,
                UserStatus status, UserRole role) {

        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.pin = pin;
        this.kycStatus = kycStatus;
        this.status = status;
        this.role = role;

        this.wrongPinAttempts = 0;
        this.transactionLockedUntil = null;
    }

    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public KycStatus getKycStatus() {
        return kycStatus;
    }

    public UserStatus getStatus() {
        return status;
    }

    public UserRole getRole() {
        return role;
    }

    public int getWrongPinAttempts() {
        return wrongPinAttempts;
    }

    public LocalDateTime getTransactionLockedUntil() {
        return transactionLockedUntil;
    }

    public boolean verifyPin(String enteredPin) {
        return pin.equals(enteredPin);
    }

    public void incrementWrongPinAttempts() {
        wrongPinAttempts++;
    }

    public void resetWrongPinAttempts() {
        wrongPinAttempts = 0;
    }

    public void lockTransactions() {
        transactionLockedUntil = LocalDateTime.now().plusHours(24);
        wrongPinAttempts = 0;
    }

    public boolean isTransactionLocked() {
        return transactionLockedUntil != null
                && LocalDateTime.now().isBefore(transactionLockedUntil);
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public void setKycStatus(KycStatus kycStatus) {
        this.kycStatus = kycStatus;
    }
}
