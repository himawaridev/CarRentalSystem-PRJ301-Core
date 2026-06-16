package com.carrental.model;

import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Maps to dbo.Users table.
 */
public class User {
    private int userId;
    private String username;
    private String email;
    private String passwordHash;
    private String fullName;
    private String phone;
    private String address;
    private String identityNumber;
    private String status;
    private String bankCode;
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountHolder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User() {}

    public User(int userId, String username, String email, String fullName) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
    }

    // Getters and setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getIdentityNumber() { return identityNumber; }
    public void setIdentityNumber(String identityNumber) { this.identityNumber = identityNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getBankCode() { return bankCode; }
    public void setBankCode(String bankCode) { this.bankCode = normalize(bankCode); }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = normalize(bankName); }

    public String getBankAccountNumber() { return bankAccountNumber; }
    public void setBankAccountNumber(String bankAccountNumber) { this.bankAccountNumber = normalize(bankAccountNumber); }

    public String getBankAccountHolder() { return bankAccountHolder; }
    public void setBankAccountHolder(String bankAccountHolder) { this.bankAccountHolder = normalizeAccountHolder(bankAccountHolder); }

    public boolean hasRefundBankInfo() {
        return !isBlank(bankCode)
                && !isBlank(bankName)
                && !isBlank(bankAccountNumber)
                && !isBlank(bankAccountHolder);
    }

    public boolean getRefundBankInfo() {
        return hasRefundBankInfo();
    }

    public boolean isBankInfoLocked() {
        return !isBlank(bankAccountNumber);
    }

    public boolean getBankInfoLocked() {
        return isBankInfoLocked();
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "User{userId=" + userId + ", username='" + username + "', fullName='" + fullName + "'}";
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeAccountHolder(String value) {
        String normalized = normalize(value);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
