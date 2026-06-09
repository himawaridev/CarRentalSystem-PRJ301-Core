package com.carrental.service;

import java.math.BigDecimal;

public class PaymentLinkStatusResponse {
    private long orderCode;
    private BigDecimal amount;
    private BigDecimal amountPaid;
    private BigDecimal amountRemaining;
    private String status;
    private String providerPaymentRef;
    private String rawResponse;

    public long getOrderCode() { return orderCode; }
    public void setOrderCode(long orderCode) { this.orderCode = orderCode; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }

    public BigDecimal getAmountRemaining() { return amountRemaining; }
    public void setAmountRemaining(BigDecimal amountRemaining) { this.amountRemaining = amountRemaining; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getProviderPaymentRef() { return providerPaymentRef; }
    public void setProviderPaymentRef(String providerPaymentRef) { this.providerPaymentRef = providerPaymentRef; }

    public String getRawResponse() { return rawResponse; }
    public void setRawResponse(String rawResponse) { this.rawResponse = rawResponse; }
}
