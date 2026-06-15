package com.carrental.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentRecord {
    private long paymentId;
    private String paymentType;
    private BigDecimal amount;
    private String paymentMethod;
    private String paymentStatus;
    private String transactionRef;
    private String note;
    private String provider;
    private String providerTransactionRef;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;

    public long getPaymentId() { return paymentId; }
    public void setPaymentId(long paymentId) { this.paymentId = paymentId; }

    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getTransactionRef() { return transactionRef; }
    public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getProviderTransactionRef() { return providerTransactionRef; }
    public void setProviderTransactionRef(String providerTransactionRef) {
        this.providerTransactionRef = providerTransactionRef;
    }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getDisplayMethod() {
        if ("PAYOS".equalsIgnoreCase(provider)) {
            return "PayOS QR";
        }
        if ("BANK_TRANSFER".equalsIgnoreCase(paymentMethod)) {
            return "Chuyen khoan";
        }
        if ("CASH".equalsIgnoreCase(paymentMethod)) {
            return "Tien mat";
        }
        if ("CARD".equalsIgnoreCase(paymentMethod)) {
            return "The";
        }
        if ("E_WALLET".equalsIgnoreCase(paymentMethod)) {
            return "Vi dien tu";
        }
        return paymentMethod == null || paymentMethod.isBlank() ? "Khac" : paymentMethod;
    }

    public String getDisplayType() {
        if ("DEPOSIT".equalsIgnoreCase(paymentType)) {
            return "Dat coc";
        }
        if ("RENTAL_PREPAID".equalsIgnoreCase(paymentType)) {
            return "Tien thue tra truoc";
        }
        if ("DRIVER_FEE_PREPAID".equalsIgnoreCase(paymentType)) {
            return "Phi tai xe tra truoc";
        }
        if ("RENTAL_BALANCE".equalsIgnoreCase(paymentType)) {
            return "Thu tien thue/tai xe con lai";
        }
        if ("EXTRA_CHARGE".equalsIgnoreCase(paymentType)) {
            return "Phi phat sinh";
        }
        if ("REFUND".equalsIgnoreCase(paymentType)) {
            return "Hoan coc";
        }
        return paymentType == null ? "" : paymentType;
    }
}
