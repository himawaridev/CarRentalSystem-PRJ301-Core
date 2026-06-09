package com.carrental.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentLinkRequest {
    private long orderCode;
    private String contractCode;
    private BigDecimal amount;
    private LocalDateTime expiredAt;

    public long getOrderCode() { return orderCode; }
    public void setOrderCode(long orderCode) { this.orderCode = orderCode; }

    public String getContractCode() { return contractCode; }
    public void setContractCode(String contractCode) { this.contractCode = contractCode; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDateTime getExpiredAt() { return expiredAt; }
    public void setExpiredAt(LocalDateTime expiredAt) { this.expiredAt = expiredAt; }
}
