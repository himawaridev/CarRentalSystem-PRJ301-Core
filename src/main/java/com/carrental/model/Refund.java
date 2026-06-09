package com.carrental.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Refund {
    private long refundId;
    private long contractId;
    private long sourcePaymentId;
    private BigDecimal depositAmount;
    private BigDecimal deductionAmount;
    private BigDecimal refundAmount;
    private String reason;
    private RefundMethod refundMethod;
    private String proofOfRefund;
    private PaymentStatus status;
    private Integer approvedByUserId;
    private Integer completedByUserId;
    private String providerRefundRef;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public long getRefundId() { return refundId; }
    public void setRefundId(long refundId) { this.refundId = refundId; }

    public long getContractId() { return contractId; }
    public void setContractId(long contractId) { this.contractId = contractId; }

    public long getSourcePaymentId() { return sourcePaymentId; }
    public void setSourcePaymentId(long sourcePaymentId) { this.sourcePaymentId = sourcePaymentId; }

    public BigDecimal getDepositAmount() { return depositAmount; }
    public void setDepositAmount(BigDecimal depositAmount) { this.depositAmount = depositAmount; }

    public BigDecimal getDeductionAmount() { return deductionAmount; }
    public void setDeductionAmount(BigDecimal deductionAmount) { this.deductionAmount = deductionAmount; }

    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public RefundMethod getRefundMethod() { return refundMethod; }
    public void setRefundMethod(RefundMethod refundMethod) { this.refundMethod = refundMethod; }

    public String getProofOfRefund() { return proofOfRefund; }
    public void setProofOfRefund(String proofOfRefund) { this.proofOfRefund = proofOfRefund; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public Integer getApprovedByUserId() { return approvedByUserId; }
    public void setApprovedByUserId(Integer approvedByUserId) { this.approvedByUserId = approvedByUserId; }

    public Integer getCompletedByUserId() { return completedByUserId; }
    public void setCompletedByUserId(Integer completedByUserId) { this.completedByUserId = completedByUserId; }

    public String getProviderRefundRef() { return providerRefundRef; }
    public void setProviderRefundRef(String providerRefundRef) { this.providerRefundRef = providerRefundRef; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
