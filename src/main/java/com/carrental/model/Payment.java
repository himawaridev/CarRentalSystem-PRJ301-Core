package com.carrental.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {
    private long paymentId;
    private long contractId;
    private Long paymentTransactionId;
    private Long sourcePaymentId;
    private PaymentType paymentType;
    private BigDecimal amount;
    private String paymentMethod;
    private PaymentStatus paymentStatus;
    private LocalDateTime paidAt;
    private Integer receivedByUserId;
    private String transactionRef;
    private String note;
    private LocalDateTime createdAt;

    public long getPaymentId() { return paymentId; }
    public void setPaymentId(long paymentId) { this.paymentId = paymentId; }

    public long getContractId() { return contractId; }
    public void setContractId(long contractId) { this.contractId = contractId; }

    public Long getPaymentTransactionId() { return paymentTransactionId; }
    public void setPaymentTransactionId(Long paymentTransactionId) { this.paymentTransactionId = paymentTransactionId; }

    public Long getSourcePaymentId() { return sourcePaymentId; }
    public void setSourcePaymentId(Long sourcePaymentId) { this.sourcePaymentId = sourcePaymentId; }

    public PaymentType getPaymentType() { return paymentType; }
    public void setPaymentType(PaymentType paymentType) { this.paymentType = paymentType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

    public Integer getReceivedByUserId() { return receivedByUserId; }
    public void setReceivedByUserId(Integer receivedByUserId) { this.receivedByUserId = receivedByUserId; }

    public String getTransactionRef() { return transactionRef; }
    public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
