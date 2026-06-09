package com.carrental.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentTransaction {
    private long paymentTransactionId;
    private long contractId;
    private String provider;
    private String providerTransactionRef;
    private Long providerOrderCode;
    private String providerPaymentRef;
    private BigDecimal amount;
    private PaymentStatus status;
    private String qrPayload;
    private String providerCheckoutUrl;
    private String providerQrCode;
    private LocalDateTime expiredAt;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;

    public long getPaymentTransactionId() { return paymentTransactionId; }
    public void setPaymentTransactionId(long paymentTransactionId) { this.paymentTransactionId = paymentTransactionId; }

    public long getContractId() { return contractId; }
    public void setContractId(long contractId) { this.contractId = contractId; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getProviderTransactionRef() { return providerTransactionRef; }
    public void setProviderTransactionRef(String providerTransactionRef) { this.providerTransactionRef = providerTransactionRef; }

    public Long getProviderOrderCode() { return providerOrderCode; }
    public void setProviderOrderCode(Long providerOrderCode) { this.providerOrderCode = providerOrderCode; }

    public String getProviderPaymentRef() { return providerPaymentRef; }
    public void setProviderPaymentRef(String providerPaymentRef) { this.providerPaymentRef = providerPaymentRef; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public String getQrPayload() { return qrPayload; }
    public void setQrPayload(String qrPayload) { this.qrPayload = qrPayload; }

    public String getProviderCheckoutUrl() { return providerCheckoutUrl; }
    public void setProviderCheckoutUrl(String providerCheckoutUrl) { this.providerCheckoutUrl = providerCheckoutUrl; }

    public String getProviderQrCode() { return providerQrCode; }
    public void setProviderQrCode(String providerQrCode) { this.providerQrCode = providerQrCode; }

    public LocalDateTime getExpiredAt() { return expiredAt; }
    public void setExpiredAt(LocalDateTime expiredAt) { this.expiredAt = expiredAt; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
